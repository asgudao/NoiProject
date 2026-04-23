public class test {
    public static void main(String[] args) throws Exception {
        java.io.FileInputStream fis = new java.io.FileInputStream("test.txt");
        byte[] data = fis.readAllBytes();
        System.out.println(data.length);
    }
}
