package org.bjm.tests;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.bjm.utils.ImageUtil;

/**
 *
 * @author singh
 */
public class NameIconMain {
    
    public static void main(String[] args){
        File file=new File("/home/singh/Desktop/UB.png");
        BufferedImage profileBufferedImage=ImageUtil.drawIcon(150, "UB");
        try {
            ImageIO.write(profileBufferedImage, "PNG", file);
        } catch (IOException ex) {
            Logger.getLogger(NameIconMain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
