package org.bjm.tests.UserRegister;

import jakarta.faces.context.FacesContext;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.bjm.collections.Access;
import org.bjm.collections.User;
import org.bjm.dtos.UserDto;
import org.bjm.utils.ImageUtil;
import org.junit.jupiter.api.Test;

/**
*
*/


/**
 * Had to write this test because when the User attempts to register,if no profile Image is uploaded then no data gets 
 * populated in the database.
 * Need to perform the following steps:
 * 1) Initialise UserDto and set all values except the profile Image and the byte[] image field
 * 2) Call Simulate the code of processData in the UserRegisterMBean
 * 3) Assert the Profile Image is created using the first chars from First Name and Last Name of the User.
 * 4) Finally simulate the population of data in Access Collection  
 * 
 * @author singh
 */

public class ProfileImageTestOnUserDto {
    
    @Test
    public void testUserRegister() throws IOException{
        
        UserDto userDto=new UserDto();
        Access access = new Access();
        User user =new User();
        
        //Simupate the data entry on the WebPage
        userDto.setEmail("topiwala@gmail.com");
        userDto.setFirstName("Topi");
        userDto.setLastName("Wala");
        userDto.setGender("Male");
        userDto.setDob("27/11/1975");
        userDto.setMobile("07795141311");
        userDto.setStateCode("CH");
        userDto.setLokSabha("Chandigarh");
        userDto.setVidhanSabha("");
        //Not setting the profilw image. Instead the Logo should be created using the first chars from FN and LN
        
        char[] chars = new char[2];
        String imageSizeStr="150";
        String imageFormat="png";
        int imageSize=Integer.parseInt(imageSizeStr);
        chars[0]= userDto.getFirstName().charAt(0);
        chars[1]= userDto.getLastName().charAt(0);
        String text=new String(chars);
        BufferedImage profileBufferedImage=ImageUtil.drawIcon(imageSize, text);
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        ImageIO.write(profileBufferedImage, imageFormat, baos);
        ImageIO.write(profileBufferedImage, text, new File("/home/singh/Desktop/"+text+"."+imageFormat));
        baos.flush();
        byte[] jpgData=baos.toByteArray();
        baos.close();
        userDto.setProfileFile(text+"."+imageFormat);
        System.out.println("Profile File is :"+userDto.getProfileFile());
        userDto.setImage(jpgData);
        System.out.println("Image byte contents are :"+userDto.getImage().length);
    
    }
    
}
