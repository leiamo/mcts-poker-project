package bots;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

import bots.demobots.SimpleBot;

import com.biotools.meerkat.Player;

public class BotRepositoryTest {
	@Test
	public void testGetBot() {
		BotRepository botRepository = new BotRepository();
		Set<String> botNames = botRepository.getBotNames();
		assertTrue(botNames.contains("DemoBot/SimpleBot.csv"));

		BotMetaData botMetaData = botRepository.getBotMetaData("DemoBot/SimpleBot.csv");
		assertEquals("DemoBot/SimpleBot.csv", botMetaData.getBotName());
		assertEquals("bots.demobots.SimpleBot.csv", botMetaData.getBotClassName());
		assertNotNull(botMetaData.getBotPreferences());
	}

	@Test
	public void testCreateBot() {
		BotRepository botRepository = new BotRepository();
		Player bot = botRepository.createBot("DemoBot/SimpleBot.csv");
		assertNotNull(bot);
		assertTrue(bot instanceof SimpleBot);
	}
}
