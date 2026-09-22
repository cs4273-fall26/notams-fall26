/**
 * Temporary program used to test the API Layer.
 *
 * It sends a start and end location to NmsApiClient and displays
 * a short preview of each raw FAA response.
 */
public class ApiLayerDemo {

    public static void main(String[] args) {

        /*
         * Use locations entered in the terminal.
         * If none are entered, use KOKC and KDFW as examples.
         */
        String startLocation = args.length >= 1 ? args[0] : "KOKC";
        String endLocation = args.length >= 2 ? args[1] : "KDFW";

        try {
            // Create the client using credentials from environment variables.
            NmsApiClient client = NmsApiClient.fromEnvironment();

            System.out.println(
                    "Requesting NOTAMs for "
                            + startLocation + " to " + endLocation + "..."
            );

            // Send both locations to the API Layer.
            RawRouteResponses responses =
                    client.fetchNotamsForRoute(
                            startLocation,
                            endLocation
                    );

            // Display a short preview to confirm raw JSON was returned.
            printResponse(
                    responses.getStartLocation(),
                    responses.getStartResponse()
            );

            printResponse(
                    responses.getEndLocation(),
                    responses.getEndResponse()
            );

            System.out.println("API Layer test completed successfully.");

        } catch (NmsApiException exception) {
            System.err.println(
                    "API Layer error: " + exception.getMessage()
            );

            // Display the status code only when the FAA returned one.
            if (exception.getStatusCode() != -1) {
                System.err.println(
                        "HTTP status: " + exception.getStatusCode()
                );
            }
        } catch (IllegalArgumentException exception) {
            System.err.println(
                    "Configuration error: " + exception.getMessage()
            );
        }
    }

    /**
     * Displays the response size and its first 300 characters.
     *
     * The full raw response remains available to the future Parsing Layer.
     */
    private static void printResponse(
            String location,
            String rawResponse) {

        System.out.println();
        System.out.println("Location: " + location);
        System.out.println(
                "Raw response length: "
                        + rawResponse.length() + " characters"
        );

        int previewLength =
                Math.min(300, rawResponse.length());

        System.out.println("Response preview:");
        System.out.println(
                rawResponse.substring(0, previewLength)
        );

        if (rawResponse.length() > previewLength) {
            System.out.println("...");
        }
    }
}