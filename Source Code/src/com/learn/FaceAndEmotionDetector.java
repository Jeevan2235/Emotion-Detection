package com.learn;

import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

@SuppressWarnings("unused")
public class FaceAndEmotionDetector extends JFrame {

    private static final long serialVersionUID = 1L;
    private JLabel imageLabel;
    private Mat image;

    public FaceAndEmotionDetector() {
        super("Face and Emotion Detection");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Load OpenCV library
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        // Read an image from file
        image = Imgcodecs.imread("images/faces.jpg");

        // Create GUI components
        JPanel buttonPanel = new JPanel();
        JButton faceButton = new JButton("Face Detection");
        JButton emotionButton = new JButton("Emotion Detection");
        imageLabel = new JLabel();

        // Set layout and add components to the frame
        setLayout(new BorderLayout());
        add(buttonPanel, BorderLayout.SOUTH);
        add(new JScrollPane(imageLabel), BorderLayout.CENTER);

        buttonPanel.add(faceButton);
        buttonPanel.add(emotionButton);

        // Add action listeners to the buttons
        faceButton.addActionListener(e -> detectAndDisplayFaces());
        emotionButton.addActionListener(e -> detectAndDisplayEmotions());

        // Set frame properties and make it visible
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setVisible(true);
    }

    // Method to detect faces in the image
    private void detectAndDisplayFaces() {
        // Load the classifier for face detection
        MatOfRect faces = new MatOfRect();
        CascadeClassifier faceCascade = new CascadeClassifier();
        faceCascade.load("data/haarcascade_frontalface_alt2.xml");

        // Detect faces in the image
        faceCascade.detectMultiScale(image, faces, 1.1, 2, 0 | Objdetect.CASCADE_SCALE_IMAGE, new Size(30, 30), new Size());

        // Draw rectangles around detected faces
        for (Rect faceRect : faces.toArray()) {
            Imgproc.rectangle(image, faceRect, new Scalar(0, 0, 255), 3);
        }

        // Update the displayed image
        updateImage();
    }

    // Method to detect emotions based on facial expressions
    private void detectAndDisplayEmotions() {
        // Load the classifier for face detection
        MatOfRect faces = new MatOfRect();
        CascadeClassifier faceCascade = new CascadeClassifier();
        faceCascade.load("data/haarcascade_frontalface_alt2.xml");

        // Detect faces in the image
        faceCascade.detectMultiScale(image, faces, 1.1, 2, 0 | Objdetect.CASCADE_SCALE_IMAGE, new Size(30, 30), new Size());

        // Process each detected face
        for (Rect faceRect : faces.toArray()) {
            // Extract the region of interest (mouth area)
            Rect mouthRect = new Rect(faceRect.x, faceRect.y + faceRect.height / 2, faceRect.width, faceRect.height / 2);
            Mat mouthROI = new Mat(image, mouthRect);

            // Calculate intensity to determine emotion
            Scalar mouthMean = Core.mean(mouthROI);
            double mouthIntensity = mouthMean.val[0];

            String emotionMessage;
            String youtubeLink;

            // Check mouth intensity to determine emotion
            if (mouthIntensity < 200) {
                emotionMessage = "Don't be sad. Here are some songs to make you feel better.";
                youtubeLink = "https://www.youtube.com/watch?v=ZbZSe6N_BXs";

                // Overlay "Sad" text on the image
                Imgproc.putText(image, "Sad", new Point(faceRect.x, faceRect.y + 90),
                        Imgproc.FONT_HERSHEY_SIMPLEX, 5.5, new Scalar(0, 0, 255), 5);
            } else {
                emotionMessage = "Great. You're happy. Here are some songs to enlighten your mood even more.";
                youtubeLink = "https://www.youtube.com/watch?v=ZbZSe6N_BXs";

                // Overlay "Happy" text on the image
                Imgproc.putText(image, "Happy", new Point(faceRect.x, faceRect.y + 90),
                        Imgproc.FONT_HERSHEY_SIMPLEX, 5.5, new Scalar(5, 255, 0), 5);
            }

            // Create labels for emotion message and YouTube link
            JLabel emotionLabel = new JLabel(emotionMessage);
            JLabel youtubeLabel = createHyperlinkLabel(youtubeLink);

            // Create a panel to display emotion message and YouTube link
            JPanel emotionPanel = new JPanel(new BorderLayout());
            emotionPanel.add(emotionLabel, BorderLayout.CENTER);
            emotionPanel.add(youtubeLabel, BorderLayout.SOUTH);

            // Show the emotion detection result in a dialog
            JOptionPane.showMessageDialog(this, emotionPanel, "Emotion Detection Result", JOptionPane.PLAIN_MESSAGE);
        }

        // Update the displayed image
        updateImage();
    }

    // Method to create a clickable hyperlink label
    private JLabel createHyperlinkLabel(String url) {
        JLabel label = new JLabel("<html><a href='" + url + "'>Click here for songs</a></html>");
        label.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Add mouse and keyboard listeners to open the URL on click or enter press
        label.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                try {
                    Desktop.getDesktop().browse(new URL(url).toURI());
                } catch (IOException | URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        });

        label.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    try {
                        Desktop.getDesktop().browse(new URL(url).toURI());
                    } catch (IOException | URISyntaxException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        return label;
    }

    // Method to update the displayed image
    private void updateImage() {
        BufferedImage bufferedImage = matToBufferedImage(image);
        if (bufferedImage != null) {
            ImageIcon icon = new ImageIcon(bufferedImage.getScaledInstance(600, 400, Image.SCALE_DEFAULT));
            imageLabel.setIcon(icon);
            repaint();
        }
    }

    // Method to convert a Mat to BufferedImage
    private BufferedImage matToBufferedImage(Mat matrix) {
        if (matrix.width() <= 0 || matrix.height() <= 0) {
            return null; // Return null for invalid dimensions
        }

        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (matrix.channels() > 1) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        int bufferSize = matrix.channels() * matrix.cols() * matrix.rows();
        byte[] buffer = new byte[bufferSize];
        matrix.get(0, 0, buffer);
        BufferedImage image = new BufferedImage(matrix.cols(), matrix.rows(), type);
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(buffer, 0, targetPixels, 0, buffer.length);
        return image;
    }

    public static void main(String[] args) {
        // Run the application
        SwingUtilities.invokeLater(() -> new FaceAndEmotionDetector());
    }
}
