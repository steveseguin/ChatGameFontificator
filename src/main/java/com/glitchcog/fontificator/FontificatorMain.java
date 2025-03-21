package com.glitchcog.fontificator;

import java.io.IOException;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import com.glitchcog.fontificator.bot.ChatViewerBot;
import com.glitchcog.fontificator.bot.SocialStreamHttpServer;
import com.glitchcog.fontificator.config.FontificatorProperties;
import com.glitchcog.fontificator.gui.chat.ChatWindow;
import com.glitchcog.fontificator.gui.controls.ControlWindow;
import com.glitchcog.fontificator.gui.controls.panel.LogBox;

/**
 * Houses the main method for the program
 * 
 * @author Matt Yanos
 */
public class FontificatorMain
{
    private static final Logger logger = Logger.getLogger(FontificatorMain.class);

    public final static PatternLayout LOG_PATTERN_LAYOUT = new PatternLayout("[%p] %d{MM-dd-yyyy HH:mm:ss} %c %M - %m%n");

	/**
	 * Setup the Social Stream HTTP server to receive messages from Social Stream Ninja
	 * 
	 * @param bot The ChatViewerBot to send messages to
	 * @param port The port to listen on
	 * @param fProps The properties containing configuration
	 */
	private static void setupSocialStreamHttpServer(ChatViewerBot bot, int port, FontificatorProperties fProps) {
		try {
			if (bot == null) {
				logger.error("Cannot setup SocialStreamHttpServer: ChatViewerBot is null");
				return;
			}
			
			// Ensure bot has message config
			if (bot.getMessageConfig() == null && fProps != null) {
				bot.setMessageConfig(fProps.getMessageConfig());
				logger.info("Set MessageConfig for ChatViewerBot");
			}
			
			SocialStreamHttpServer server = new SocialStreamHttpServer(port, bot);
			server.start();
			logger.info("Successfully started SocialStreamHttpServer on port " + port);
		} catch (IOException e) {
			logger.error("Failed to start Social Stream HTTP server", e);
		} catch (Exception e) {
			logger.error("Unexpected error setting up Social Stream HTTP server", e);
		}
	}

    /**
     * The main method for the program
     * 
     * @param args
     *            unused
     * @throws Exception
     */
    public static void main(String[] args)
    {
        // Configure the logger
        BasicConfigurator.configure(new ConsoleAppender(LOG_PATTERN_LAYOUT));
        Logger.getRootLogger().setLevel(Level.INFO);

        try
        {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        }
        catch (Exception e)
        {
            logger.error(e.toString(), e);
        }

        LogBox logBox = new LogBox();

        // These properties contain all the configuration for the program
        FontificatorProperties fProps = new FontificatorProperties();

        // The ChatWindow is the main window and shows the visualization of the chat
        ChatWindow chatWindow = new ChatWindow();

        // The ControlWindow is the dependent window that has all the options for modifying the properties of the chat
        final ControlWindow controlWindow = new ControlWindow(chatWindow, fProps, logBox);

        // Attempt to load the last opened data, or fall back to defaults if nothing has been loaded or if there are any
        // errors loading
        controlWindow.loadLastData(chatWindow);

        try
        {
            // Feed the properties into the chat to give it hooks into the properties' configuration models; Feed the
            // ControlWindow into the ChatWindow to give the chat hooks back into the controls; Sets the loaded member
            // Boolean in the chat to indicate it has everything it needs to begin rendering the visualization
            chatWindow.initChat(fProps, controlWindow);
        }
        catch (Exception e)
        {
            logger.error(e.toString(), e);
            ChatWindow.popup.handleProblem(e.toString(), e);
            System.exit(1);
        }

        // Build the GUI of the control window
        controlWindow.build(logBox);

        // Load after init takes care of the (mostly chat window based) configurations that require the window be already set up
        controlWindow.loadAfterInit();

        // Give the chat panel the message dialog so it can read censorship rules and call for the manual censorship
        // list to be redrawn when a message is posted
        chatWindow.getChatPanel().setMessageCensor(controlWindow.getMessageDialog().getCensorPanel());

        // Give the debug tab to the chat panel, since it doesn't have a shared reference to a config object for the settings
        chatWindow.getChatPanel().setDebugSettings(controlWindow.getDebugPanel());
        
        // Initialize ChatBot for the chat panel
        if (chatWindow.getChatPanel().getChatBot() == null) {
            logger.info("Creating new ChatViewerBot for ChatPanel");
            ChatViewerBot chatBot = new ChatViewerBot();
            chatBot.setChatPanel(chatWindow.getChatPanel());
            chatWindow.getChatPanel().setChatBot(chatBot);
        }
        
        // Setup Social Stream HTTP server to receive messages from Social Stream Ninja
        logger.info("Setting up SocialStreamHttpServer...");
		setupSocialStreamHttpServer(chatWindow.getChatPanel().getChatBot(), 8888, fProps);

        // Finally, display the chat and control windows now that everything has been constructed and connected
        chatWindow.setVisible(true);
        try
        {
            // Do it ugly but thread safe
            SwingUtilities.invokeAndWait(new Runnable()
            {
                @Override
                public void run()
                {
                    controlWindow.setVisible(true);
                }
            });
        }
        catch (Exception e)
        {
            logger.error("Unable to display control window on initialization", e);
        }
    }
}