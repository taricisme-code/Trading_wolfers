import com.tradingdemo.util.PasswordUtils;

public class TestHash {
    public static void main(String[] args) {
        String testPassword = "admin123";
        String testHash = "$2a$12$jHqHaLaKn14OeBJF6ILCyOvZ.JnsU0aXYXM9PJGJzJ9dn.FBIvXS.";
        
        boolean matches = PasswordUtils.verifyPassword(testPassword, testHash);
        System.out.println("Testing password: " + testPassword);
        System.out.println("Testing hash: " + testHash);
        System.out.println("Password matches: " + (matches ? "YES ✓" : "NO ✗"));
        
        if (!matches) {
            System.out.println("\nGenerating new hash for password...");
            String newHash = PasswordUtils.hashPassword(testPassword);
            System.out.println("New hash: " + newHash);
            System.out.println("New hash verifies: " + PasswordUtils.verifyPassword(testPassword, newHash));
        }
    }
}
