import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class StockTicker2 {

    public static final int STOCKS_NUM = 10;  // max. number of stocks allowed
    public static final int DATA_TYPES = 3;   // number of data types

    public static void main(String[] args) throws IOException, InterruptedException {

        Scanner console = new Scanner(System.in);       // input console

        String[] tickers = new String[STOCKS_NUM];      // stock tickers

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

        // Builds the request with the ticker(s) per Yahoo Finance API for the stock quotes
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://yfapi.net/v6/finance/quote?region=US&lang=en&symbols=" + urlTickers))
                .header("x-api-key", "U6bD4zA5MCu2SRZChaWfjXBDDA2mWqGl") // Replace with your actual API key
                .GET()
                .build();

        // Sends the request to Yahoo Finance and receives the stock quote
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Parses the JSON response
        JsonObject[] stockData = parseJsonResponse(tickers, numOfStocks, response.body());

        // Prints the stock info in the output table
        printStockData(tickers, dataTypes, numOfStocks, stockData, dataTypesDisplay);
    }

    // Prompts the user for the number of stocks interested, returning the number of stocks
    public static int Intro(Scanner console) {

        // Program info
        System.out.println("Hello user! Enter one or more stock tickers and this program will show you the relevant information.");
        System.out.println("If you enter more than one ticker, the information will be compared between the stocks.");

        // Prompts for the number of stocks
        System.out.print("Number of stocks: ");
        int numOfStocks = console.nextInt();

        // Caps the number of stocks allowed
        if (numOfStocks > STOCKS_NUM) {
            numOfStocks = STOCKS_NUM;
            System.out.println("Only " + STOCKS_NUM + " stocks are allowed.");
        }

        return numOfStocks;
    }

    // Prompts the user for the stock ticker(s), returning one string for the stock ticker(s)
    public static String getUrlTickers(Scanner console, int numOfStocks, String[] tickers) {

        // Prompts the user
        if (numOfStocks == 1) {
            System.out.println("Please enter " + numOfStocks + " ticker:");
        } else {
            System.out.println("Please enter " + numOfStocks + " tickers:");
        }

        // Prompts for stock ticker(s)
        for (int i = 0; i < numOfStocks; i++) {

            // Prompts for each stock ticker
            System.out.print("Stock ticker " + (i + 1) + ": ");
            tickers[i] = console.next();

            // Converts to upper case since the user might enter the ticker as lower case
            tickers[i] = tickers[i].toUpperCase();
        }

        StringBuilder urlTickers = new StringBuilder(tickers[0]);

        // For multiple stock tickers, "%2C" is the separator between stock tickers.
        if (numOfStocks > 1) {
            for (int j = 1; j < numOfStocks; j++) {
                urlTickers.append("%2C").append(tickers[j]);
            }
        }

        return urlTickers.toString();
    }

    // Parses the JSON response and returns an array of JsonObjects containing stock data
    public static JsonObject[] parseJsonResponse(String[] tickers, int numOfStocks, String responseBody) {
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);

        JsonArray results = jsonObject.getAsJsonObject("quoteResponse").getAsJsonArray("result");
        JsonObject[] stockData = new JsonObject[numOfStocks];

        for (int i = 0; i < numOfStocks; i++) {
            boolean found = false;
            for (int j = 0; j < results.size(); j++) {
                JsonObject stock = results.get(j).getAsJsonObject();
                if (stock.get("symbol").getAsString().equals(tickers[i])) {
                    stockData[i] = stock;
                    found = true;
                    break;
                }
            }
            if (!found) {
                System.out.printf("%s is not a valid ticker or data is unavailable.\n", tickers[i]);
                stockData[i] = null;
            }
        }
        return stockData;
    }

    // Prints the stock info in a table for comparison
    public static void printStockData(String[] tickers, String[] dataTypes, int numOfStocks, JsonObject[] stockData, String[] dataTypesDisplay) {

        // Header row
        System.out.printf("%-22s", " ");
        for (int i = 0; i < numOfStocks; i++) {
            System.out.printf("%8s", tickers[i]);
        }
        System.out.println();

        // Print each data type for all stock tickers
        for (int i = 0; i < DATA_TYPES; i++) {
            // First column is data type
            System.out.printf("%-22s", dataTypesDisplay[i]);

            // For all stock tickers
            for (int j = 0; j < numOfStocks; j++) {
                if (stockData[j] != null && stockData[j].has(dataTypes[i])) {
                    try {
                        double dataValue = stockData[j].get(dataTypes[i]).getAsDouble();
                        System.out.printf("%8.2f", dataValue);
                    } catch (Exception e) {
                        // Handle cases where the data is not a double or is null
                        System.out.printf("%8s", "N/A");
                    }
                } else {
                    // If data is not available, print N/A
                    System.out.printf("%8s", "N/A");
                }
            }
            System.out.println();
        }
    }
}
