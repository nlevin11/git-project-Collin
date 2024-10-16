
import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.security.*;
import java.util.zip.*;
import java.nio.file.*;


public class Blob {
    private static String file;
    private static String hashedFileContent;
    private static File forObjectsFolder;
    private static String fileContent;

    public static boolean zip = false; // by default it zips the data

    public Blob(String fileName) throws IOException {

        File file2 = new File (fileName);
        //file = fileName;
        if(!file2.exists())
        {
            throw new FileNotFoundException("File does not exist");
        }
        this.file = fileName;
        fileContent = getFileContent(fileName);
        hashedFileContent = toSHA1(fileContent);
        String sha1 =null;
        forObjectsFolder = new File("git/objects/" + hashedFileContent);
        boolean isTree = true;
        
        //checks if tree
        if (!file2.isDirectory())
        {
            isTree = false;
        }
       
        try {
            if (!forObjectsFolder.exists() && !file2.isDirectory()) {
                forObjectsFolder.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //writes to index 
        
        
        try (FileWriter writer = new FileWriter("git/index", true)) {
            if (isTree == false)
            {
                

                writer.write("blob " + hashedFileContent + " " + file2.getPath() + "\n");

                try (FileWriter objectWriter = new FileWriter("git/objects/" + hashedFileContent)) 
                {
                     objectWriter.write(getFileContent(fileName));
                }

            }
            else
            {
                //add all the files and 
                String treeHash = addDirectoryToIndex(new File (fileName), writer, fileName);
            }
        } 
         catch (IOException e) 
        {
            e.printStackTrace();
        }

    }
    //adding all files to index froma tree
    private String addDirectoryToIndex(File folder, FileWriter writer, String path) throws IOException
    {
        if (!folder.exists())
        {
            throw new FileNotFoundException("File does not exist");
        }
        File [] files = folder.listFiles();
        StringBuilder treeContent = new StringBuilder();
        if (files == null)
        {
            return null;
        }
        for (File f : files)
        {
            if (f.isDirectory())
            {

                String treeSha1 = addDirectoryToIndex(f, writer, f.getPath());

                treeContent.append ("tree " + treeSha1 + " " + f.getName() + "\n"); 
            }
            else
            {

                String blobSha1 = new Blob(f.getPath()).hashedFileContent;
                //writer.write("blob " + blobSha1 + " " + path + "/" + f.getName() + "\n");
                treeContent.append ("blob " + blobSha1 + " " + f.getPath() + "\n");

            }
        }
        String treeSha1 = toSHA1(treeContent.toString());
        writer.write("tree " + treeSha1 + " " + path + "\n");
          File treeObjectFile = new File("git/objects/" + treeSha1);
            if (!treeObjectFile.exists())
            {
                treeObjectFile.createNewFile();
            }
            FileWriter objectWriter = new FileWriter(treeObjectFile);
            objectWriter.write (treeContent.toString());
            objectWriter.close();
                
 
        return treeSha1;

    }


    public String toSHA1(String content) { // mostly from stack overflow:
                             // https://stackoverflow.com/questions/4895523/java-string-to-sha1
        if (zip) { // compress the string (this is pseudocode for now bc havent written it yet)
            content = compress();
        }

        String sha1 = "";
        try {
            MessageDigest encrypter = MessageDigest.getInstance("SHA-1");
            encrypter.reset();
            encrypter.update(content.getBytes("UTF-8"));
            sha1 = byteToHex(encrypter.digest());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sha1;
    }


    public String getFileContent(String fileName) {
      String content =  "";
        try{
        content = new String (Files.readString(Path.of(fileName)));

       }catch (Exception e)
       {

       }
       return content;
    }

    private static String byteToHex(final byte[] hash) { // shamelessly copied from stack overflow:
                                                         // https://stackoverflow.com/questions/4895523/java-string-to-sha1
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }

    public static boolean toggleZip() {
        if (zip) {
            zip = false;
            return false;
        } else {
            zip = true;
            return true;
        }
    }

    public static boolean getZip() {
        return zip;
    }

    public String compress() {
        byte[] input; // https://docs.oracle.com/javase/8/docs/api/java/util/zip/Deflater.html
        byte[] output = new byte[1000];
        try {
            // Encode a String into bytes
            input = fileContent.getBytes("UTF-8");

            // Compress the bytes
            Deflater compresser = new Deflater();
            compresser.setInput(input);
            compresser.finish();
            //System.out.println (compresser.finished()); //for testing
            int compressedDataLength = compresser.deflate(output); //not used but may be necessary?
            compresser.end();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return new String(output);

        // bad code:
        // zip fileContent
        // inspiration:
        // https://stackoverflow.com/questions/63885313/is-there-a-way-to-zip-a-plain-text-file-from-a-string-in-java
        // String zippedContent = "";
        // try {
        // ZipOutputStream zipped = new ZipOutputStream(new
        // FileOutputStream(fileContent));
        // zippedContent = zipped.toString();
        // zipped.close();
        // } catch (Exception e) {
        // e.printStackTrace();
        // }
        // return zippedContent;

        /*
         * StringBuilder sb = new StringBuilder(fileContent);
         * File f = new File("c:\\payload.zip");
         * ZipOutputStream out = new ZipOutputStream(new FileOutputStream(f));
         * ZipEntry e = new ZipEntry("myFile.txt");
         * out.putNextEntry(e);
         * byte[] data = sb.toString().getBytes();
         * out.write(data, 0, data.length);
         * out.closeEntry();
         * out.close();
         */
    }

}
