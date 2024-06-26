package org.bjm.utils;

/**
 *
 * @author singh
 */
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

public class ConvertPngToJpg {
    public static byte[] convertToJpg(byte[] pngData){
        byte[] jpgData=null;
        ByteArrayInputStream bais=new ByteArrayInputStream(pngData);
        ByteArrayOutputStream baos=null;
        try {
            BufferedImage pngBI=ImageIO.read(bais);
            BufferedImage jpgBI = new BufferedImage(
                    pngBI.getWidth(),
                    pngBI.getHeight(),
                    BufferedImage.TYPE_INT_RGB);
            jpgBI.createGraphics().drawImage(pngBI, 0, 0, Color.WHITE, null);
            baos=new ByteArrayOutputStream();
            ImageIO.write(jpgBI, "jpg", baos);
            jpgData=baos.toByteArray();
        } catch (IOException ex) {
            Logger.getLogger(ConvertPngToJpg.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jpgData;
        
        
    }
}
