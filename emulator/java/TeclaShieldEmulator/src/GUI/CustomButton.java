package GUI;

import java.awt.Graphics;
import java.awt.Image;
import javax.swing.JButton;

/**
 *
 * @author Rishabh
 */
public class CustomButton extends JButton{

    private Image pressedImage,releasedImage;
    
    public CustomButton(){
        super();
    } 
    
    public CustomButton(String label){
        super(label);
    }

    public void setPressedImage(Image pressedImage) {
        this.pressedImage = pressedImage;
    }

    public void setReleasedImage(Image releasedImage) {
        this.releasedImage = releasedImage;
    }
    
    @Override
    public void paintComponents(Graphics g) {
        super.paintComponents(g);
        boolean selected = isSelected();
        if(selected){
            g.drawImage(pressedImage, 0, 0, null);
        }
        else{
            g.drawImage(releasedImage, 0, 0, null);
        }
    }
    
}
