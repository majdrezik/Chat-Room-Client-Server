package com.packagename.chat;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.PWA;

import reactor.core.publisher.Flux;
import reactor.core.publisher.UnicastProcessor;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * A sample Vaadin view class.
 * <p>
 * To implement a Vaadin view just extend any Vaadin component and
 * use @Route annotation to announce it in a URL as a Spring managed
 * bean.
 * Use the @PWA annotation make the application installable on phones,
 * tablets and some desktop browsers.
 * <p>
 * A new instance of this class is created for every new user and every
 * browser tab/window.
 */
@SuppressWarnings("serial")
@Route("")
@Push
@PWA(name = "MajdChat",
        shortName = "MajdChat",
        description = "This is a simple chatroom.",
        enableInstallPrompt = false)
@CssImport("./styles/shared-styles.css")
@CssImport(value = "./styles/vaadin-text-field-styles.css", themeFor = "vaadin-text-field")
@StyleSheet("frontend://styles/shared-styles.css")
public class MainView extends VerticalLayout {

	private String username;
	private UnicastProcessor<ChatMessage> publisher;
	private Flux<ChatMessage> messages;
    
    public MainView(UnicastProcessor<ChatMessage> publisher, Flux<ChatMessage> messages) {
    	this.publisher = publisher;
    	this.messages = messages;
    	setDefaultHorizontalComponentAlignment(Alignment.CENTER);
    	setSizeFull(); 
    	addClassName("main-view");
    	H1 header = new H1("Majd Chat");
    	header.getElement().getThemeList().add("dark");
    	add(header);
    	askUsername();
    }

	private void askUsername() {
	HorizontalLayout usernameLayout = new HorizontalLayout();
	TextField usernameField = new TextField();
	usernameField.setPlaceholder("Username");
	usernameField.focus();
	Button startButton = new Button("Start Chatting");
	usernameLayout.add(usernameField);
	usernameLayout.add(startButton);
	
	startButton.addClickListener(click -> {
		username = usernameField.getValue();
		remove(usernameLayout);
		showChat();
	});
	
	
	add(usernameLayout);
	}

	private void showChat() {
		MessageList messageList = new MessageList();
		add(messageList,createInputLayout());
		expand(messageList);
		
		//TO ENABLE MULTI USERS AT THE SAME TIME.
		//lock the UI
		messages.subscribe(message -> {
			getUI().ifPresent(ui -> 
				ui.access(()->	//The thing that takes in the runnable
					messageList.add(new Paragraph(
							message.getFrom() + ": " + message.getMessage()
							))
					));
		});
	}

	private Component createInputLayout() {
		HorizontalLayout inputLayout = new HorizontalLayout();
		inputLayout.setWidth("100%");
		TextField messageField = new TextField();
		messageField.setPlaceholder("Type something...");
		Button sendButton = new Button("Send");
		sendButton.getElement().getThemeList().add("primary");
		inputLayout.add(messageField,sendButton);
		inputLayout.expand(messageField);
		
		sendButton.addClickListener(click -> {
			publisher.onNext(new ChatMessage(username, messageField.getValue()));
			messageField.clear(); //clear the message field and get back the placeholder.
			messageField.focus(); //focus on message field so the user can continue typing.
		});
		messageField.focus(); //focus on message field so the user can continue typing.

		return inputLayout;
	}
}
