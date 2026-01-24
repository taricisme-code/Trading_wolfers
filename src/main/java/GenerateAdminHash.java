import com.tradingdemo.util.PasswordUtils;

public class GenerateAdminHash {
    public static void main(String[] args) {
        String password = "admin123";
        String hash = PasswordUtils.hashPassword(password);
        System.out.println("Password: " + password);
        System.out.println("Hash: " + hash);
    }
}
