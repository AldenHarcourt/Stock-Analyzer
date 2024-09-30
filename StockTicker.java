import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;

// Add JSON imports
import org.json.JSONArray;
import org.json.JSONObject;

public class StockTicker {

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
      dataTypesDisplay[1] = "Forward PE Ratio";
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
             .uri(URI.create("https://yfapi.net/v6/finance/quote?region=US&lang=en&symbols=" + urlTickers))
             .header("x-api-key", "YOUR_VALID_API_KEY_HERE") // Replace with your valid API key
             .method("GET", HttpRequest.BodyPublishers.noBody())
             .build();

      // Sends the request to Yahoo Finance and receives the stock quote   
      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
      
      // Converts the stock quote data into a text string
      String quoteResponse = response.body();

      // Gets the info for each stock     
      getStockData(tickers, numOfStocks, quoteResponse, stockStrings);
      
      // Prints the stock info in the output table
      printStockData(tickers, dataTypes, numOfStocks, stockStrings, dataTypesDisplay);      
   }
   
   // ... [Intro and getUrlTickers methods remain unchanged] ...

   // Parses the stock quote data for each stock using JSON parsing
   public static void getStockData(String[] tickers, int numOfStocks, String quoteResponse, String[] stockStrings) {
  
      JSONObject jsonResponse = new JSONObject(quoteResponse);
      JSONObject quoteResponseObj = jsonResponse.getJSONObject("quoteResponse");
      JSONArray resultArray = quoteResponseObj.getJSONArray("result");

      for (int i = 0; i < numOfStocks; i++) {
          boolean found = false;
          for (int j = 0; j < resultArray.length(); j++) {
              JSONObject stockObj = resultArray.getJSONObject(j);
              if (stockObj.getString("symbol").equals(tickers[i])) {
                  stockStrings[i] = stockObj.toString();
                  found = true;
                  break;
              }
          }
          if (!found) {
              // Tells the user the ticker is not valid
              System.out.printf("%s is not a valid ticker\n", tickers[i]);
              // No info for the ticker
              stockStrings[i] = null;
          }
      }
   }
   
   // Prints the stock info in a table for comparison   
   public static void printStockData(String[] tickers, String[] dataTypes, int numOfStocks, String[] stockStrings, String[] dataTypesDisplay) {

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

               JSONObject stockObj = new JSONObject(stockStrings[j]);

               if (stockObj.has(dataTypes[i]) && !stockObj.isNull(dataTypes[i])) {
                  double data = stockObj.getDouble(dataTypes[i]);
                  
                  // Rounds to 2 digits after the decimal point
                  double roundedData = Math.round(data * 100.0) / 100.0;  
                  System.out.printf("%8.2f", roundedData);
               } else {
                  // If data is not available
                  System.out.printf("%8s", "N/A");
               }
               
            } else {
            
               // If data is not available, prints spaces
               System.out.printf("%8s", " ");             
            }
         }
         
         // New line
         System.out.println();
      }
   }

   // ... [Include unchanged methods here: Intro and getUrlTickers] ...

}
