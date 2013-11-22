package iw7i.messages.chat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/chat/{room}", encoders = ChatMessageEncoder.class, decoders = ChatMessageDecoder.class)
public class ChatEndpoint {
	private final Logger log = Logger.getLogger(getClass().getName());
	private static ArrayList<Session> sesiones = new ArrayList<Session>(10);

	@OnError
	public void error(final Session session, final Throwable err) {
		log.log(Level.SEVERE, "fallo crítico", err);
	}

	@OnOpen
	public void open(final Session session, @PathParam("room") String room) {
		log.info("open");
		session.getUserProperties().put("room", room); // guardamos la sala de
														// chat elegida
		sesiones.add(session); // añadimos la sesión a la lista de sesiones
	}

	@OnClose
	public void onClose(final Session session) {
		log.info("close");
		sesiones.remove(session); // eliminamos la sesión de la lista de
									// sesiones
	}

	@OnMessage
	public void onMessage(final Session session, final ChatMessage chatMessage) {
		log.info("message " + chatMessage);
		try {
			for (Session sesión : sesiones) {
				// enviamos el mensaje a todos los clientes en la misma sala de
				// chat que el emisor
				if (sesión.getUserProperties().get("room")
						.equals(session.getUserProperties().get("room"))) {
					sesión.getBasicRemote().sendObject(chatMessage);
				}
			}
		} catch (IOException | EncodeException e) {
			log.log(Level.WARNING, "onMessage failed", e);
		}
	}
}
