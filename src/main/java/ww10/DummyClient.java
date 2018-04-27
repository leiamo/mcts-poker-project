package ww10;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import ww10.WW10Protocol.PrologBotDescription;

public class DummyClient {

	public static void main(String[] args) {
		for (int i = 0; i < 5; i++) {
			try {
				Socket socket = new Socket("localhost", 20000);
				String n = "\n";
				PrologBotDescription msg = WW10Protocol.PrologBotDescription
						.newBuilder()
						.setId(i)
						.setName("Tias")
						.setProlog(
								"do(raise(10)) :- round(preflop), handcards(C), pair(C), !." + n
										+ "do(fold) :- round(preflop), deficit(D), bigblind(BB), D >= 1.5*BB, !." + n + "do(call) :- round(preflop), !." + n
										+ "do(fold) :- (round(flop);round(turn)), deficit(D), bigblind(BB), D >= 2*BB, !." + n
										+ "do(call) :- (round(flop);round(turn)), !." + n
										+ "do(raise(10)) :- round(river), allcards(C), handeval(C,H), (H==straight;H==flush;H==fourofakind;H==fullhouse), !."
										+ n + "do(fold) :- round(river), allcards(C), handeval(C,highcard), !." + n + "do(call) :- !.").build();
				OutputStream outputStream = socket.getOutputStream();
				InputStream inputStream = socket.getInputStream();
				msg.writeTo(outputStream);
				outputStream.close();
				inputStream.close();
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(-1);
			}
		}
	}
}
