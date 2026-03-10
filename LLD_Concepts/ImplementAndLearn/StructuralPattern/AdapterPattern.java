
/**
 * Adapter Pattern — Third-party Weather example
 *
 * Scenario:
 * - ThirdPartyWeatherService returns weather in Fahrenheit (F) and mph.
 * - Our app wants Celsius (C) and km/h.
 *
 * Demonstration:
 * 1) Without Adapter: Client manually converts the units.
 * 2) With Adapter: Client uses WeatherService (target interface) 
 *    and receives data already converted.
 */
public class AdapterPattern {
    private static String fmt(double v) { return String.format("%.2f", v); }

    public static void main(String[] args) {
        String city = "London";

        // 1) WITHOUT ADAPTER: client knows about F/mph and does conversions itself
        ThirdPartyWeatherService thirdParty = new ThirdPartyWeatherService();
        ThirdPartyWeatherData raw = thirdParty.fetchWeatherFAndMph(city);
        double tempC = Units.fToC(raw.getTempF());
        double windKmh = Units.mphToKmh(raw.getWindMph());
        System.out.println("[Without Adapter] " + city + " -> " +
                "Temp: " + fmt(tempC) + "°C, Wind: " + fmt(windKmh) + " km/h");

        // 2) WITH ADAPTER: client calls a clean interface and gets C/kmh directly
        WeatherService weather = new ThirdPartyWeatherAdapter(thirdParty);
        WeatherData data = weather.getWeather(city);
        System.out.println("[With Adapter] " + city + " -> " +
                "Temp: " + fmt(data.getTempC()) + "°C, Wind: " + fmt(data.getWindKmh()) + " km/h");
    }
}

/**
 * Target interface our app prefers to use (Celsius, km/h).
 */
interface WeatherService {
    WeatherData getWeather(String city);
}

/**
 * Target data model (Celsius, km/h).
 */
class WeatherData {
    private final double tempC;
    private final double windKmh;

    public WeatherData(double tempC, double windKmh) {
        this.tempC = tempC;
        this.windKmh = windKmh;
    }

    public double getTempC() {
        return tempC;
    }

    public double getWindKmh() {
        return windKmh;
    }

    @Override
    public String toString() {
        return String.format("WeatherData{tempC=%.2f, windKmh=%.2f}", tempC, windKmh);
    }
}

/**
 * Adaptee: Third-party service that returns Fahrenheit and mph.
 * Imagine this class comes from an external SDK that you cannot modify.
 */
class ThirdPartyWeatherService {
    public ThirdPartyWeatherData fetchWeatherFAndMph(String city) {
        // Dummy values to simulate a real API response
        // e.g., 77°F (25°C) and 12 mph (~19.31 km/h)
        double tempF = 77.0;
        double windMph = 12.0;
        return new ThirdPartyWeatherData(tempF, windMph);
    }
}

/**
 * Adaptee data model (Fahrenheit, mph).
 */
class ThirdPartyWeatherData {
    private final double tempF;
    private final double windMph;

    public ThirdPartyWeatherData(double tempF, double windMph) {
        this.tempF = tempF;
        this.windMph = windMph;
    }

    public double getTempF() {
        return tempF;
    }

    public double getWindMph() {
        return windMph;
    }
}

/**
 * Adapter: Converts from the third-party's F/mph to our app's C/kmh.
 */
class ThirdPartyWeatherAdapter implements WeatherService {
    private final ThirdPartyWeatherService adaptee;

    public ThirdPartyWeatherAdapter(ThirdPartyWeatherService adaptee) {
        this.adaptee = adaptee;
    }

    @Override
    public WeatherData getWeather(String city) {
        ThirdPartyWeatherData raw = adaptee.fetchWeatherFAndMph(city);
        double c = Units.fToC(raw.getTempF());
        double kmh = Units.mphToKmh(raw.getWindMph());
        return new WeatherData(c, kmh);
    }
}

/**
 * Unit conversion helpers.
 */
class Units {
    public static double fToC(double f) {
        return (f - 32.0) * (5.0 / 9.0);
    }

    public static double mphToKmh(double mph) {
        return mph * 1.60934;
    }
}
