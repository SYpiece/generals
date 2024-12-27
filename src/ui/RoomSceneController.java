package ui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import model.Player;
import socket.GameClient;
import socket.GameServer;
import socket.GameStatus;
import socket.event.GameClientAdapter;
import socket.event.GameClientEvent;
import ui.control.RoomConnectProgressIndicator;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ResourceBundle;

public class RoomSceneController implements Initializable {
    @FXML
    private Pane _rootPane;
    //roomPane
    private final boolean _isHost;
    private GameServer _gameServer;
    private GameClient _gameClient;
    public RoomSceneController(boolean isHost) {
        _isHost = isHost;
    }
    @FXML
    private Node _isHostLabel;
    @FXML
    private Node _notHostLabel;
    private final GameClientAdapter _clientAdapter = new GameClientAdapter() {
        @Override
        public void gameStatusChanged(GameClientEvent event) {
            if (event.getGameStatus() != GameStatus.Preparing) {
                _gameClient.removeClientListener(_clientAdapter);
                Platform.runLater(() -> {
                    try {
                        initializeGame();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }
    };
    private void initializeGame() throws IOException {
        FXMLLoader loader = new FXMLLoader(RoomSceneController.class.getResource("initializing scene.fxml"));
        loader.setController(new InitializingSceneController(_gameClient));
        _rootPane.getScene().setRoot(loader.load());
    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (_isHost) {
            _isHostLabel.setVisible(true);
            _notHostLabel.setVisible(false);
            _roomConnectPane.setVisible(false);
            try {
                _gameServer = new GameServer(new InetSocketAddress("127.0.0.1", 44444));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try {
                _gameClient = new GameClient(new InetSocketAddress("127.0.0.1", 44444), new Player.PlayerInformation());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            _isHostLabel.setVisible(false);
            _notHostLabel.setVisible(true);
            _roomConnectPane.setVisible(true);
        }
    }
    //connectPane
    @FXML
    private TextField _ipTextField, _portTextField;
    @FXML
    private Label _ipErrorLabel, _portErrorLabel, _connectErrorLabel;
    @FXML
    private Pane _connectPaneBox, _roomConnectPane;
    @FXML
    private Button _connectButton;
    private final ProgressIndicator _connectProgressIndicator = new RoomConnectProgressIndicator();
    private Thread _connectThread;
    @FXML
    private void onCancelButtonClicked() throws IOException, InterruptedException {
        if (_connectThread != null && _connectThread.isAlive()) {
            _connectThread.interrupt();
            _connectThread.join();
        }
        FXMLLoader loader = new FXMLLoader(RoomSceneController.class.getResource("main scene.fxml"));
        _rootPane.getScene().setRoot(loader.load());
    }
    @FXML
    private void onConnectButtonClicked() {
        _ipErrorLabel.setVisible(false);
        _portErrorLabel.setVisible(false);
        InetAddress inetAddress;
        try {
            inetAddress = InetAddress.getByName(_ipTextField.getText());
        } catch (UnknownHostException e) {
            e.printStackTrace();
            _ipErrorLabel.setVisible(true);
            return;
        }
        int port;
        try {
            port = Integer.parseInt(_portTextField.getText());
            if (port < 0 || port > 0xFFFF) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            _portErrorLabel.setVisible(true);
            return;
        }
        _connectErrorLabel.setVisible(false);
        _connectPaneBox.getChildren().add(_connectProgressIndicator);
        _connectButton.setDisable(true);
        _connectThread = new Thread(() -> {
            boolean isConnected;
            try {
                _gameClient = new GameClient(new InetSocketAddress(inetAddress, port), null);
                isConnected = true;
            } catch (IOException e) {
                _gameClient = null;
                isConnected = false;
            }
            final boolean finalIsConnected = isConnected;
            Platform.runLater(() -> {
                _connectThread = null;
                _connectPaneBox.getChildren().remove(_connectProgressIndicator);
                _connectButton.setDisable(false);
                if (finalIsConnected) {
                    _roomConnectPane.setVisible(false);
                } else {
                    _connectErrorLabel.setVisible(true);
                    _connectPaneBox.getChildren().remove(_connectProgressIndicator);
                }
            });
        });
        _connectThread.start();
    }
}
