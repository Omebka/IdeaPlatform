import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Main {
    public static final String ORIGIN_NAME = "Владивосток";
    public static final String DESTINATION_NAME = "Тель-Авив";
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("d.M.yy H:m");

    public static void main(String[] args) {
        try (InputStream is = Main.class.getResourceAsStream("tickets.json")) {
            JsonObject jsonData = JsonParser.parseReader(new JsonReader(new InputStreamReader(is, StandardCharsets.UTF_8))).getAsJsonObject();
            is.close();
            JsonArray ticketsArray = (JsonArray) jsonData.get("tickets");

            int amountOfTickets = 0;
            long secondsOfFlights = 0L;
            ArrayList<Long> arrayOfSecondsOfFlights = new ArrayList<>();

            for (JsonElement ticket : ticketsArray) {
                JsonObject ticketObject = ticket.getAsJsonObject();

                String originName = ticketObject.get("origin_name").getAsString();
                String destinationName = ticketObject.get("destination_name").getAsString();

                if (originName.equals(ORIGIN_NAME) && destinationName.equals(DESTINATION_NAME)) {
                    amountOfTickets++;
                    LocalDateTime departureDateTime = getDepartureDateTime(ticketObject);
                    LocalDateTime arrivalDateTime = getArrivalDateTime(ticketObject);

                    long secondsOfFlight = Duration.between(departureDateTime, arrivalDateTime).abs().getSeconds();
                    secondsOfFlights += secondsOfFlight;
                    arrayOfSecondsOfFlights.add(secondsOfFlight);
                }
            }

            if (amountOfTickets == 0) {
                System.out.println("Нет билетов из '" + ORIGIN_NAME + "' в '" + DESTINATION_NAME + "'.");
            } else {
                long averageFlightTimeSeconds = secondsOfFlights / amountOfTickets;
                String averageFlightTime = secondsToHoursAndMinutes(averageFlightTimeSeconds);
                System.out.println("Среднее время: " + averageFlightTime);

                double percentile = 90;
                long percentileOfFlightTimeSeconds = percentileOfArrayOfLongs(arrayOfSecondsOfFlights, percentile);
                String percentileOfFlightTime = secondsToHoursAndMinutes(percentileOfFlightTimeSeconds);
                System.out.println(percentile + " процентиль: " + percentileOfFlightTime);
            }
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static @NotNull LocalDateTime parseDateAndTime(String date, String time) {
        return LocalDateTime.parse(date + " " + time, FORMATTER);
    }

    private static @NotNull LocalDateTime getDepartureDateTime(@NotNull JsonObject jsonObject) {
        String departureDate = jsonObject.get("departure_date").getAsString();
        String departureTime = jsonObject.get("departure_time").getAsString();

        return parseDateAndTime(departureDate, departureTime);
    }

    private static @NotNull LocalDateTime getArrivalDateTime(@NotNull JsonObject jsonObject) {
        String arrivalDate = jsonObject.get("arrival_date").getAsString();
        String arrivalTime = jsonObject.get("arrival_time").getAsString();

        return parseDateAndTime(arrivalDate, arrivalTime);
    }

    private static String secondsToHoursAndMinutes(long seconds) {
        return String.format("%d:%02d", seconds / 3600, (seconds % 3600) / 60);
    }

    private static long percentileOfArrayOfLongs(List<Long> array, double percentile) {
        Collections.sort(array);

        return array.get((int) percentile * array.size() / 100);
    }
}
