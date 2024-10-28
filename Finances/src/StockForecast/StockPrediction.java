package StockForecast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class StockPrediction {

        public static void main(String[] args) {
                try {
                        // Fetch stock data
                        String symbol = "SPY";
                        List<StockData> stockData = fetchStockData(symbol);

                        if (stockData.isEmpty()) {
                                System.out.println("No stock data available.");
                                return;
                        }

                        // Calculate estimated highs and lows for tomorrow
                        double estimatedHigh = calculateEstimatedHigh(stockData);
                        double estimatedLow = calculateEstimatedLow(stockData);

                        // Calculate EMA and Bollinger Bands
                        double ema = calculateEMA(stockData, 5);
                        double[] bollingerBands = calculateBollingerBands(stockData, 20); // Use 20-day period for Bollinger Bands

                        // Print estimated values for tomorrow and technical indicators
                        System.out.printf("Estimated High for Tomorrow: %.2f%n", estimatedHigh);
                        System.out.printf("Estimated Low for Tomorrow: %.2f%n", estimatedLow);
                        System.out.printf("5-Day EMA: %.2f%n", ema);
                        System.out.printf("Bollinger Bands: Upper - %.2f, Lower - %.2f%n", bollingerBands[0], bollingerBands[1]);

                        // Forecast for the next week
                        forecastNextWeek(stockData, estimatedHigh, estimatedLow);

                } catch (Exception e) {
                        e.printStackTrace();
                }
        }

        private static List<StockData> fetchStockData(String symbol) throws Exception {
                String apiKey = "MI6B6S1NW06QI5I0"; // Replace with your Alpha Vantage API key
                String urlString = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=" + symbol + "&apikey=" + apiKey + "&outputsize=full";

                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                // Read the API response
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                }
                in.close();

                // Extract and print only the relevant section
                String timeSeriesKey = "\"Time Series (Daily)\":";
                int startIndex = response.indexOf(timeSeriesKey);
                if (startIndex != -1) {
                        String relevantData = response.substring(startIndex);
                        System.out.println("Time Series Data: " + relevantData);
                } else {
                        System.out.println("Time Series data not found in the response.");
                }

                // Parse the JSON response to extract stock data
                return parseStockData(response.toString());
        }


        private static List<StockData> parseStockData(String jsonResponse) {
                List<StockData> stockDataList = new ArrayList<>();
                String timeSeriesKey = "\"Time Series (Daily)\":";
                int startIndex = jsonResponse.indexOf(timeSeriesKey);

                if (startIndex != -1) {
                        startIndex += timeSeriesKey.length();
                        int endIndex = jsonResponse.indexOf("}", startIndex) + 1;

                        // Extract the time series JSON object
                        String timeSeriesJson = jsonResponse.substring(startIndex, endIndex);
                        String[] entries = timeSeriesJson.split("\\},\\{");

                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

                        for (String entry : entries) {
                                try {
                                        // Clean up the entry
                                        entry = entry.replaceAll("[{}]", ""); // Remove braces
                                        String[] parts = entry.split(","); // Split by comma

                                        String dateStr = parts[0].split(":")[0].replaceAll("\"", "").trim(); // Get the date
                                        Date date = dateFormat.parse(dateStr);

                                        // Collect all available data
                                        double open = Double.parseDouble(parts[1].split(":")[1].replaceAll("\"", "").trim());
                                        double close = Double.parseDouble(parts[3].split(":")[1].replaceAll("\"", "").trim());
                                        double volume = Double.parseDouble(parts[4].split(":")[1].replaceAll("\"", "").trim());

                                        stockDataList.add(new StockData(open, close, volume));
                                } catch (Exception e) {
                                        // Print an error message if needed
                                        System.out.println("Error parsing entry: " + entry);
                                }
                        }
                }

                // Only take the last 20 valid data points
                List<StockData> recentData = new ArrayList<>();
                for (int i = stockDataList.size() - 1; i >= 0; i--) {
                        recentData.add(stockDataList.get(i));
                        if (recentData.size() == 20) break; // Get only the last 20 days
                }

                System.out.println("Number of valid stock data points: " + recentData.size());
                return recentData;
        }


        private static double calculateEMA(List<StockData> stockData, int period) {
                if (stockData.size() < period) {
                        System.out.println("Not enough data to calculate EMA. Required: " + period + ", Available: " + stockData.size());
                        return Double.NaN;
                }

                double multiplier = 2.0 / (period + 1);
                double ema;

                // Calculate initial EMA using the first `period` closing prices
                double initialEmaSum = 0;
                for (int i = stockData.size() - period; i < stockData.size(); i++) {
                        initialEmaSum += stockData.get(i).close;
                }

                // Initial EMA is the average of the first `period` closing prices
                ema = initialEmaSum / period;

                // Calculate EMA for the rest of the closing prices
                for (int i = stockData.size() - period; i < stockData.size(); i++) {
                        ema = (stockData.get(i).close - ema) * multiplier + ema;
                }

                return ema;
        }

        private static double[] calculateBollingerBands(List<StockData> stockData, int period) {
                if (stockData.size() < period) return new double[]{Double.NaN, Double.NaN};

                double sum = 0;
                for (int i = stockData.size() - period; i < stockData.size(); i++) {
                        sum += stockData.get(i).close;
                }
                double average = sum / period;

                double sumSquaredDifferences = 0;
                for (int i = stockData.size() - period; i < stockData.size(); i++) {
                        double diff = stockData.get(i).close - average;
                        sumSquaredDifferences += diff * diff;
                }
                double stdDev = Math.sqrt(sumSquaredDifferences / period);

                double upperBand = average + (2 * stdDev);
                double lowerBand = average - (2 * stdDev);
                return new double[]{upperBand, lowerBand};
        }

        private static double calculateRSI(List<StockData> stockData, int period) {
                if (stockData.size() < period) return Double.NaN;

                double gainSum = 0;
                double lossSum = 0;

                for (int i = stockData.size() - period; i < stockData.size(); i++) {
                        double change = stockData.get(i).close - stockData.get(i - 1).close;
                        if (change > 0) {
                                gainSum += change;
                        } else {
                                lossSum -= change; // Loss is negative, so subtract to get positive value
                        }
                }

                double avgGain = gainSum / period;
                double avgLoss = lossSum / period;

                if (avgLoss == 0) return 100; // Avoid division by zero

                double rs = avgGain / avgLoss;
                return 100 - (100 / (1 + rs));
        }

        private static double calculateEstimatedHigh(List<StockData> stockData) {
                double sumHigh = 0;
                int count = Math.min(stockData.size(), 5); // Use last 5 days

                for (int i = stockData.size() - count; i < stockData.size(); i++) {
                        sumHigh += stockData.get(i).close; // Use the close price
                }

                double avgHigh = (count == 0) ? Double.NaN : (sumHigh / count);
                double rsi = calculateRSI(stockData, 14);

                // Adjust estimated high based on RSI
                if (rsi > 70) {
                        return avgHigh * 0.99; // Assume a slight decrease if overbought
                } else {
                        return avgHigh * 1.01; // Assume a 1% increase otherwise
                }
        }

        private static double calculateEstimatedLow(List<StockData> stockData) {
                double sumLow = 0;
                int count = Math.min(stockData.size(), 5); // Use last 5 days

                for (int i = stockData.size() - count; i < stockData.size(); i++) {
                        sumLow += stockData.get(i).open; // Use the open price
                }

                double avgLow = (count == 0) ? Double.NaN : (sumLow / count);
                double rsi = calculateRSI(stockData, 14);

                // Adjust estimated low based on RSI
                if (rsi < 30) {
                        return avgLow * 1.01; // Assume a slight increase if oversold
                } else {
                        return avgLow * 0.99; // Assume a 1% decrease otherwise
                }
        }

        private static void forecastNextWeek(List<StockData> stockData, double initialEstimatedHigh, double initialEstimatedLow) {
                // Use last estimated values as starting point
                double lastEstimatedHigh = initialEstimatedHigh;
                double lastEstimatedLow = initialEstimatedLow;

                System.out.println("Forecast for the Next Week:");

                // Loop through each day of the week (5 days)
                for (int day = 1; day <= 5; day++) {
                        // Estimate high and low for the day
                        lastEstimatedHigh = calculateEstimatedHighWithPrevious(stockData, lastEstimatedHigh, lastEstimatedLow, true);
                        lastEstimatedLow = calculateEstimatedLowWithPrevious(stockData, lastEstimatedHigh, lastEstimatedLow, false);

                        // Print the estimates for the day
                        System.out.printf("Estimated High for Day %d: %.2f%n", day, lastEstimatedHigh);
                        System.out.printf("Estimated Low for Day %d: %.2f%n", day, lastEstimatedLow);

                        // Update stock data with new estimated values for the next iteration
                        stockData.add(new StockData(lastEstimatedLow, lastEstimatedHigh, 0)); // Volume is not needed here
                }
        }

        private static double calculateEstimatedHighWithPrevious(List<StockData> stockData, double lastHigh, double lastLow, boolean isHigh) {
                double rsi = calculateRSI(stockData, 14);
                double changeFactor = (rsi > 70) ? 0.99 : 1.01; // Decrease if overbought, otherwise increase
                return lastHigh * changeFactor; // Adjust based on RSI
        }

        private static double calculateEstimatedLowWithPrevious(List<StockData> stockData, double lastHigh, double lastLow, boolean isLow) {
                double rsi = calculateRSI(stockData, 14);
                double changeFactor = (rsi < 30) ? 1.01 : 0.99; // Increase if oversold, otherwise decrease
                return lastLow * changeFactor; // Adjust based on RSI
        }

        static class StockData {
                double open;
                double close;
                double volume;

                public StockData(double open, double close, double volume) {
                        this.open = open;
                        this.close = close;
                        this.volume = volume;
                }
        }
}
