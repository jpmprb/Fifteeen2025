package pt.ipbeja.app.ui;

import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import pt.ipbeja.app.model.Position;

import java.net.URL;
import java.nio.file.Paths;


/**
 * This button (knows its position in the grid)
 *
 * @author João Paulo Barros
 * @version 2025/05/12
 * images generated in https://text2image.com/en/
 */
public class PieceButton extends Button
{
    private final Position position;

    public PieceButton(String text, Position position)
    {
        super(text);
        this.position = position;
        this.setTextAndImage(text);
    }

    /**
     * @return the position
     */
    public Position position()
    {
        return this.position;
    }

    /**
     * sets the text and image for the button
     *
     * @param newText
     *            to set
     */
    public void setTextAndImage(String newText)
    {
        this.setText(newText);
        String imageName = newText;
        if (newText.isEmpty())
        {
            imageName = "empty";
        }
        // The path should start with a '/' to indicate it's relative to the root of the classpath
        String imagePath = "/images/" + imageName + ".png";

        URL imageUrl = getClass().getResource(imagePath);

        if (imageUrl != null) {
            Image image = new Image(imageUrl.toExternalForm());
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(50);
            imageView.setFitHeight(50);
            //imageView.setPreserveRatio(true);

            this.setGraphic(imageView);
        } else {
            System.err.println("Cannot find resource: " + imagePath);
            this.setGraphic(null);
            return;
        }
    }
}
