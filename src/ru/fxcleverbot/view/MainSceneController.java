package ru.fxcleverbot.view;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import ru.fxcleverbot.Main;
import ru.fxcleverbot.controller.CleverParser;
import ru.fxcleverbot.view.control.Message;

public class MainSceneController{

    @FXML
    public TextField messageTextField;

    @FXML
    public Button sendButton;

    @FXML
    public VBox messages;

    @FXML
    public ScrollPane scrollPane;

    @FXML
    public ProgressIndicator loadingProgressIndicator;

    @FXML
    public void initialize(){
        Thread thread = new Thread(() -> {
            try{
                CleverParser.getInstance().init(); //Initialize parser

                //Remove progress indicator from pane
                Platform.runLater(() -> {
                    ((Pane) this.loadingProgressIndicator.getParent()).getChildren().remove(this.loadingProgressIndicator);
                    this.loadingProgressIndicator = null;

                    this.sendButton.setDisable(false);
                    this.messageTextField.setDisable(false);
                });
            }catch(Exception exception){
                exception.printStackTrace();

                Platform.runLater(() ->
                        this.showErrorAlert(
                                "An error occurred while loading the bot",
                                exception.getClass().getName() + ": " + exception.getMessage()
                        ));

                Platform.exit();
            }
        });
        thread.start();

        this.scrollPane.vvalueProperty().bind(this.messages.heightProperty());
    }

    @FXML
    public void onSendButtonPressed(){
        final String text = this.messageTextField.getText().trim();

        if(!text.equals("")){
            this.sendButton.setDisable(true);
            this.messageTextField.setText("");
            this.messageTextField.setDisable(true);

            this.addMessage(
                    new Image(Main.class.getResource("view/image/boy.png").toString()),
                    text,
                    false
            );

            Thread thread = new Thread(() -> {
                try{
                    final String answer = CleverParser.getInstance().sendAI(text);

                    Platform.runLater(() -> {
                        this.addMessage(
                                new Image(Main.class.getResource("view/image/support.png").toString()),
                                answer,
                                true
                        );

                        this.sendButton.setDisable(false);
                        this.messageTextField.setDisable(false);
                    });
                }catch(Exception exception){
                    exception.printStackTrace();

                    Platform.runLater(() ->
                            this.showErrorAlert(
                                    "An error occurred while send message!",
                                    exception.getClass().getName() + ": " + exception.getMessage()
                            )
                    );
                }
            });

            thread.start();
        }else{
            this.showErrorAlert("Type message...", null);
        }
    }

    @FXML
    public void onMessageTextFieldKeyReleased(KeyEvent keyEvent){
        if(keyEvent.getCode() == KeyCode.ENTER){
            this.onSendButtonPressed();
        }
    }

    private void addMessage(Image image, String text, boolean second){
        Message message = new Message(this.scrollPane);
        message.setMessage(text);
        message.setAvatar(image);

        if(second)
            message.changeOrientation();

        this.messages.getChildren().add(message);
    }

    private void showErrorAlert(String header, String content){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}