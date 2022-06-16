import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

//
// Displays the real-time stock info
//
//    This program asks the user to input the number of the stock(s) and stock ticker(s),
//    retrieves the real-time stock market data from Yahoo Finance, and displays 
//    three pieces of info for each stock: stock price, PE ratio and price to book ratio.
//
//    Features: allows up to 10 stocks in one query at a time.
//
//    Limitation: only equity is supported; mutual funds or ETF funds are not.
//

public class StockAnalyzer {

   public static final int STOCKS_NUM = 10;  // max. number of stocks allowed
   public static final int DATA_TYPES = 3;   // number of data types
    
   public static void main(String[] args) throws IOException, InterruptedException {
       
      Scanner console = new Scanner(System.in);       // input console
      
      String[] tickers = new String[STOCKS_NUM];      // stock tickers
      String[] stockStrings = new String[STOCKS_NUM]; // stock info
      
      String[] dataTypes = new String[DATA_TYPES];    // stock data type
      dataTypes[0] = "regularMarketPrice";            // stock price per share
      dataTypes[1] = "forwardPE";                     // forward Price Earning (PE) ratio
      dataTypes[2] = "priceToBook";                   // price to book ratio
      
      // Descriptions in the output table
      String[] dataTypesDisplay = new String[DATA_TYPES];
      dataTypesDisplay[0] = "Price Per Share";
      dataTypesDisplay[1] = "Foward PE Ratio";
      dataTypesDisplay[2] = "Price To Book Ratio";

      // Prompts the user for the number of stocks            
      int numOfStocks = Intro(console);

      // Prompts the user for the stock ticker(s)
      String urlTickers = getUrlTickers(console, numOfStocks, tickers);

      // Gets real-time stock data from Yahoo Finance       
      HttpClient client = HttpClient.newHttpClient();    // Establishes a new HTTP client

      // Builds the request with the ticker(s) per Yahoo Finance API for the stock quotes,
      // the API key is assigned when registering for the Yahoo Finance API service.     
      HttpRequest request = HttpRequest.newBuilder()
             .uri(URI.create("https://yfapi.net/v6/finance/quote?region=US&lang=en&symbols=" 
                             + urlTickers))
             .header("x-api-key", "RB2ABbDBWB9lZaeez6pR570YzT6QNfjD4aTo4cQU")
             .method("GET", HttpRequest.BodyPublishers.noBody())
             .build();

      // Sends the request to Yahoo Finance and receives the stock quote   
      HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
      
      // Converts the stock quote data into a text string
      String quoteResponse = response.body().toString();

      // Gets the info for each stock     
      getStockData(tickers, numOfStocks, quoteResponse, stockStrings);
      
      // Prints the stock info in the output table
      printStockData(tickers, dataTypes, numOfStocks, stockStrings, dataTypesDisplay);      
   }
   
   // Prompts the user for the number of stocks interested, returnning the number of stocks
   public static int Intro(Scanner console) {
     
      // Program info
      System.out.println("Hello user! Enter one or more stock tickers and this program will " + 
                         "show you the relevent information.");
      System.out.println("If you enter more than one ticker the information will be compared" + 
                         " between the other stocks.");

      // Prompts for the number of stocks 
      System.out.print("Number of stocks: ");
      int numOfStocks = console.nextInt();
 
      // Caps the number of stocks allowed
      if ( numOfStocks >= STOCKS_NUM) {
      
         numOfStocks = STOCKS_NUM;
         
         System.out.println("Only 10 stocks are allowed.");     
      }
    
      return numOfStocks;   
   }

   // Prompts the user for the stock ticker(s), returning one string for the stock ticker(s)
   public static String getUrlTickers(Scanner console, int numOfStocks, String[] tickers) {

      // Prompts the user   
      if (numOfStocks == 1) {
         System.out.println("Please enter " + numOfStocks + " ticker ");
      }
      else {
         System.out.println("Please enter " + numOfStocks + " tickers ");
      }

      // Prompts for stock ticker(s)          
      for (int i = 0; i < numOfStocks; i++) {

         // Prompts for each stock ticker        
         System.out.print("Stock ticker " + (i + 1) + " : ");
         tickers[i] = console.next();
         
         // Converts to upper case since the user might enter the ticker as lower case,
         // the stock ticker from Yahoo Finance is in upper case
         tickers[i] = tickers[i].toUpperCase();
      }
   
      String urlTickers = tickers[0];
 
      // For multiple stock tickers, "2C" is the separator between stock tickers. 
      // Only one string with multiple stock tickers separated by "2C" is sent to
      // Yahoo Finance.     
      if (numOfStocks > 1) {
      
         for (int j = 1; j < numOfStocks; j++) {
            urlTickers = urlTickers + "%2C" + tickers[j];
         }     
      }
      
      return urlTickers;   
   }

   // Parses the stock quote data for each stock
   public static void getStockData(String[] tickers, int numOfStocks, String quoteResponse, 
                                   String[] stockStrings) {
  
      String tempNewQuoteResponse = quoteResponse;
      int endIndex = 0;

      // Replaces ":" char in the quote data with a space
      tempNewQuoteResponse = quoteResponse.replaceAll(":", " ");

      // Replaces "," char in the quote data with a space
      tempNewQuoteResponse = tempNewQuoteResponse.replaceAll(",", " ");
      
      for (int i = 0; i < numOfStocks; i++) {
      
         // Finds the quote string for the given ticker
         endIndex = tempNewQuoteResponse.indexOf("\"symbol\" " + "\"" + tickers[i] + "\"");
         
         if (endIndex != -1) {
         
            // The ticker is found and extractx the stock's info
            stockStrings[i] = tempNewQuoteResponse.substring(0, endIndex);

            // Obtains the remaining quote string for the next ticker
            tempNewQuoteResponse = tempNewQuoteResponse.substring(endIndex);

         } else {
         
            // Tells the user the ticker is not valid
            System.out.printf("%s is not a valid ticker\n", tickers[i]);
            
            // No info for the ticker
            stockStrings[i] = null;
         }   
      }
   }
   
   // Prints the stock info in a table for comparison   
   public static void printStockData(String[] tickers, String[] dataTypes, int numOfStocks, 
                                     String[] stockStrings, String[] dataTypesDisplay) {

      // 1st column is 22 spaces
      System.out.printf("%22s", " ");
      
      // The rest of columns are 8 spaces right aligned
      for(int i = 0; i < numOfStocks; i++) {
         System.out.printf("%8s",tickers[i]);             
      }
      
      // New line
      System.out.println();

      // Prints each data type for all stock tickers
      for(int i = 0; i < DATA_TYPES; i++) {

         // 1st column is data type 22 spaces left aligned for each data type
         System.out.printf("%-22s", dataTypesDisplay[i]);
         
         // For all stock tickers                     
         for (int j = 0; j < numOfStocks; j++) {
         
            if (stockStrings[j] != null) {
            
               // Data is available and get the data for the data type
               String sub = stockStrings[j].substring(stockStrings[j].indexOf(dataTypes[i]) 
                                                      + dataTypes[i].length() + 2);
 
               // Extracts the data and converts to float number
               String temp = sub.substring(0, sub.indexOf(" "));
               double data = Double.parseDouble(temp);
              
               // Rounds to 2 digits after the decimal point
               double roundedData = Math.round(data * 100.0) / 100.0;  
            
               // The rest of columns are 8 spaces right aligned; 
               // two digits after the decimal point for the numbers
               System.out.printf("%8.2f", roundedData);
               
            } else {
            
               // If data is not available, prints spaces
               System.out.printf("%8s", " ");             
            }
         }
         
         // New line
         System.out.println();
      }
   }
}