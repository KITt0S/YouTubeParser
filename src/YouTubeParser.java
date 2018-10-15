import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class YouTubeParser {

    public static void main(String[] args) {

        String fileName = "youtube_video.xlsx";
        try {

            XSSFWorkbook workbook = new XSSFWorkbook( new FileInputStream( fileName ) );
            XSSFSheet mainSheet = workbook.getSheet( "лист1" );
            List<String> urlList = new ArrayList<>();
            for (int i = 1; i < mainSheet.getPhysicalNumberOfRows(); i++) {

                String cellData = mainSheet.getRow( i ).getCell( 1 ).getStringCellValue();
                mainSheet.getRow( i ).createCell( 2 );
                mainSheet.getRow( i ).getCell( 2 ).setCellValue( "" );
                String[] urls = cellData.split( ";" );
                for ( String s :
                     urls ) {

                    urlList.add( s.trim() );
                }
            }
            List<UrlParser> urlParsers = new ArrayList<>();
            for (String url :
                    urlList ) {

                urlParsers.add( new UrlParser( url ) );
            }
            ExecutorService service = Executors.newFixedThreadPool( 3 );

            outer:      for ( UrlParser p : urlParsers ) {

                String numberOfVideos = service.submit( p ).get();
                if(  numberOfVideos != null ) {

                    for (int i = 1; i < mainSheet.getPhysicalNumberOfRows(); i++) {

                        Row row = mainSheet.getRow( i );

                        if( row.getCell( 1 ).getStringCellValue().equals( p.getUrl() ) ) {

                            if( row.getCell( 2 ).getStringCellValue().equals( "" ) ) {

                                row.createCell( 2 );
                            }
                            row.getCell( 2 ).setCellValue( numberOfVideos );
                            continue outer;
                        } else if( row.getCell( 1 ).getStringCellValue().contains( p.getUrl() ) ) {

                            if( row.getCell( 2 ).getStringCellValue().equals( "" ) ) {

                                row.createCell( 2 );
                                row.getCell( 2 ).setCellValue( numberOfVideos );
                            } else {

                                int numberOfUrls = row.getCell(1).getStringCellValue().split(";").length;
                                int numberOfViewCount = row.getCell(2).getStringCellValue().split("\n").length;

                                if (numberOfViewCount > 0 && numberOfViewCount < numberOfUrls) {

                                    row.getCell(2).setCellValue(row.getCell(2).getStringCellValue() + "\n" +
                                            numberOfVideos);
                                } else if (numberOfViewCount == numberOfUrls) {

                                    row.getCell(2).setCellValue(numberOfVideos);
                                }
                            }
                        }
                    }

                }
            }
            OutputStream fileOutput = new FileOutputStream( fileName );
            workbook.write( fileOutput );
            fileOutput.flush();
            fileOutput.close();
            service.shutdown();
            JOptionPane.showMessageDialog(null, "Программа завершила свою работу!",
                    "Сообщение", JOptionPane.INFORMATION_MESSAGE );
        } catch ( IOException e ) {

            JOptionPane.showMessageDialog(null, "Закройте файл youtube_video.xlsx и" +
                            " запустите программу снова!", "Внимание!", JOptionPane.WARNING_MESSAGE );
        } catch ( ExecutionException | InterruptedException e ) {

            e.printStackTrace();
        }
    }

}
