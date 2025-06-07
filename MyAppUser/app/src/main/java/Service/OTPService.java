package Service;

import java.util.Random;

public class OTPService {
    private static final Random RANDOM = new Random();
    private static final int OTP_LENGTH = 4;

    // Method to generate OTP
    public String generateOtp() {
        int otp = 1000 + RANDOM.nextInt(9000); // Generates a 4-digit OTP
        return String.valueOf(otp);
    }
}
