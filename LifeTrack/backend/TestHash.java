import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TestHash {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
        String pass = "admin123";
        boolean match = encoder.matches(pass, hash);
        System.out.println("Matches: " + match);
        System.out.println("New Hash: " + encoder.encode(pass));
    }
}
