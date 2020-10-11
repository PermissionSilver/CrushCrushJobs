import java.text.NumberFormat;
import java.util.Locale;
import java.util.Random;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class Jobs extends Application {
	public static void main(String[] args) { launch(); }
	
	@Override
	public void start(Stage stage) {
		Group root = new Group();
		Scene scene = new Scene(root, 800, 610);
		
		ComboBox[] selections = new ComboBox[18];
		Label[] selLabels = new Label[18];
		ToggleButton[] gilded = new ToggleButton[18];
		for (int i = 0, j; i < 9; i++)
			for (j = 0; j < 2; j++) {
				selLabels[i * 2 + j] = new Label(jobs[i * 2 + j]);
				root.getChildren().add(selLabels[i * 2 + j]);
				selLabels[i * 2 + j].setLayoutX(13 + j * 240);
				selLabels[i * 2 + j].setLayoutY(13 + i * 60);
				
				gilded[i * 2 + j] = new ToggleButton("□");
				root.getChildren().add(gilded[i * 2 + j]);
				gilded[i * 2 + j].setPrefSize(30, 30);
				gilded[i * 2 + j].setLayoutX(10 + j * 240);
				gilded[i * 2 + j].setLayoutY(30 + i * 60);
				
				selections[i * 2 + j] = new ComboBox<>(FXCollections.observableArrayList(titles[i * 2 + j]));
				root.getChildren().add(selections[i * 2 + j]);
				selections[i * 2 + j].setPrefSize(200, 30);
				selections[i * 2 + j].setLayoutX(40 + j * 240);
				selections[i * 2 + j].setLayoutY(30 + i * 60);
				selections[i * 2 + j].getSelectionModel().selectFirst();
			}
		
		Spinner<Integer> tb = new Spinner<>(0, 255, 0, 1);
		root.getChildren().add(tb);
		tb.setPrefSize(150, 30);
		tb.setLayoutX(10);
		tb.setLayoutY(570);
		tb.setEditable(true);
		tb.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		
		Spinner<Integer> gb = new Spinner<>();
		SpinnerValueFactory<Integer> gbvf = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 13, 0, 1);
		gbvf.setConverter(new StringConverter<Integer>() {
			@Override
			public String toString(Integer object) {
				int i = 1, j;
				for (j = 0; j < object; j++)
					i *= 2;
				return String.valueOf(i);
			}
			
			@Override
			public Integer fromString(String string) {
				int i = 0, j;
				for (j = Integer.valueOf(string); j > 1; j /= 2)
					i++;
				return i;
			}
		});
		gb.setValueFactory(gbvf);
		root.getChildren().add(gb);
		gb.setPrefSize(150, 30);
		gb.setLayoutX(170);
		gb.setLayoutY(570);
		gb.setEditable(true);
		gb.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		
		Spinner<Double> rb = new Spinner<>(1, 2048, 0, 0.01);
		root.getChildren().add(rb);
		rb.setPrefSize(150, 30);
		rb.setLayoutX(330);
		rb.setLayoutY(570);
		rb.setEditable(true);
		rb.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		
		Label tbl = new Label("Time Blocks");
		root.getChildren().add(tbl);
		tbl.setLayoutX(13);
		tbl.setLayoutY(553);
		
		Label gbl = new Label("Gem Boost");
		root.getChildren().add(gbl);
		gbl.setLayoutX(173);
		gbl.setLayoutY(553);
		
		Label rbl = new Label("Reset Boost");
		root.getChildren().add(rbl);
		rbl.setLayoutX(333);
		rbl.setLayoutY(553);
		
		ListView<String> output = new ListView<>();
		root.getChildren().add(output);
		output.setPrefSize(300, 452);
		output.setLayoutX(490);
		output.setLayoutY(10);
		
		Label tbu = new Label("Uses 0 Time Blocks");
		root.getChildren().add(tbu);
		tbu.setLayoutX(493);
		tbu.setLayoutY(470);
		
		Label me = new Label("Earns $0 per second");
		root.getChildren().add(me);
		me.setLayoutX(493);
		me.setLayoutY(490);
		
		stage.setTitle("Job Selection Optimiser");
		stage.setOnCloseRequest(e -> System.exit(0));
		stage.setScene(scene);
		stage.sizeToScene();
		stage.setResizable(false);
		stage.show();
		
		new Thread(() -> {
			int[] levels = new int[18];
			double[] plevels = new double[21];
			plevels[18] = -1;
			plevels[19] = -1;
			plevels[20] = -1;
			boolean[] gilds = new boolean[18], pgilds = new boolean[18];
			
			while (true) {
				try {Thread.sleep(1);} catch (Exception ex) {}
				
				Platform.runLater(() -> {
					int i;
					boolean same = true;
					for (i = 0; i < 18; i++) {
						levels[i] = selections[i].getSelectionModel().getSelectedIndex();
						gilds[i] = gilded[i].isSelected();
						
						if (levels[i] != plevels[i] || gilds[i] != pgilds[i]) {
							plevels[i] = levels[i];
							pgilds[i] = gilds[i];
							same = false;
						}
						
						gilded[i].setText((gilds[i]) ? "■" : "□");
					}
					
					if (!same || plevels[18] != tb.getValue() || plevels[19] != gb.getValue() || plevels[20] != rb.getValue()) {
						plevels[18] = tb.getValue();
						plevels[19] = gb.getValue();
						plevels[20] = rb.getValue();
						
						boolean[] vals = optimise(levels, gilds, tb.getValue());
						ObservableList<String> items = FXCollections.observableArrayList();
						int b = 0;
						double p = 0;
						
						for (i = 0; i < 18; i++)
							if (vals[i]) {
								items.add(jobs[i]);
								p += Integer.valueOf(gbvf.getConverter().toString(gb.getValue())) * rb.getValue() * values[i][levels[i]][0] * ((gilds[i]) ? 5.0 : 1.0) / values[i][levels[i]][1];
								b += values[i][levels[i]][2];
							}
						
						if (items.size() == 0)
							items.add("");
						output.setItems(items);
						
						tbu.setText("Uses " + b + " Time Blocks");
						me.setText("Earns " + NumberFormat.getCurrencyInstance(Locale.US).format(p) + " per second");
					}
				});
			}
		}).start();
	}
	
	private static boolean[] optimise(int[] levels, boolean[] gilds, int timeBlocks) {
		double p = Double.NEGATIVE_INFINITY, n;
		int i;
		boolean[][] members = new boolean[1024][18];
		for (i = 0; i < 1024; i++)
			members[i][i % 18] = i % 19 > 0;
		
		for (int change = 0; change < 1000; change++) {
			n = nextGen(members, levels, gilds, timeBlocks, 0.05);
			if (n > p) {
				p = n;
				change = 0;
			}
		}
		
		for (i = 0; fitness(members[i], levels, gilds, timeBlocks) < p; i++);
		
		return members[i];
	}
	
	private static double nextGen(boolean[][] members, int[] levels, boolean[] gilds, int timeBlocks, double mu) {
		Random rnd = new Random();
		double r, m = Double.NEGATIVE_INFINITY;
		
		double[] score = new double[members.length];
		for (int i = 0; i < members.length; i++) {
			score[i] = fitness(members[i], levels, gilds, timeBlocks);
			if (score[i] > m)
				m = score[i];
		}
		
		for (int i = 0, j; i < members.length; i += 4) {
			if (score[i] < score[i + 1])
				members[i] = members[i + 1].clone();
			if (score[i + 2] < score[i + 3])
				members[i + 2] = members[i + 3].clone();
			
			for (j = 0; j < 18; j++) {
				r = rnd.nextDouble();
				members[i + 1][j] = (r < 0.5) ? members[i][j] : members[i + 2][j];
				members[i + 3][j] = (r < 0.5) ? members[i + 2][j] : members[i][j];
				if (rnd.nextDouble() < mu) {
					members[i + 1][j] = !members[i + 1][j];
					members[i + 3][j] = !members[i + 3][j];
				}
			}
		}
		
		boolean[] tmp = members[0].clone();
		for (int i = 1; i < members.length; i++)
			members[i - 1] = members[i].clone();
		members[members.length - 1] = tmp;
		
		return m;
	}
	
	private static double fitness(boolean[] member, int[] levels, boolean[] gilds, int timeBlocks) {
		double score = 0.0;
		int blocks = 0;
		
		for (int i = 0; i < 18; i++)
			if (member[i]) {
				score += values[i][levels[i]][0] * ((gilds[i]) ? 5.0 : 1.0) / values[i][levels[i]][1];
				blocks += values[i][levels[i]][2];
			}
		
		return (blocks > timeBlocks) ? -1.0 : score;
	}
	
	private static final String[] jobs = new String[]{
		"Fast Food",
		"Restaurant",
		"Cleaning",
		"Lifeguard",
		"Art",
		"Computers",
		"Zoo",
		"Hunting",
		"Casino",
		"Sports",
		"Legal",
		"Movies",
		"Space",
		"Slaying",
		"Love",
		"Wizard",
		"Grave Digger",
		"Tree Planter"
	};
	private static final String[][] titles = new String[][]{
		{
			"Locked",
			"Burger Flipper",
			"Bun Toaster",
			"Onion Rehydrater",
			"Mascot",
			"Sandwich Artist",
			"Burger Meister",
			"Meat Manager",
			"Hambaron",
			"Fry Franchiser",
			"Beef Chief"
		}, {
			"Locked",
			"Busser",
			"Waiter",
			"Server",
			"Barista",
			"Bartender",
			"Garcon",
			"Host",
			"Maitre D",
			"Shift Manager",
			"Owner"
		}, {
			"Locked",
			"Janitor",
			"Custodial Engineer",
			"Chief Sweeper",
			"Vomitorius Maximus",
			"Superintendant",
			"Groundskeeper",
			"Garbageman",
			"Undertaker",
			"Hazmat Specialist",
			"Bomb Disposal"
		}, {
			"Locked",
			"Life Guard",
			"Beach Patrol",
			"Sexy Lifeguard",
			"Surf and Protect",
			"Heli-Jumper",
			"Search and Rescue",
			"Coast Guardian",
			"Life Preserver",
			"Shark Puncher",
			"Atlantean King"
		}, {
			"Locked",
			"Artist",
			"Painter",
			"Sculptor",
			"Composer",
			"Inventor",
			"Virtuoso",
			"Renaissance Man",
			"Artiste",
			"Hype Machine",
			"Iconoclast"
		}, {
			"Locked",
			"Programmer",
			"IT Monkey",
			"Techie",
			"Hacker",
			"Engineer",
			"Computer Whisperer",
			"Cyberneticist",
			"Futurist",
			"Artificial Entity",
			"Singularity"
		}, {
			"Locked",
			"Veterinarian",
			"Puppy Rescuer",
			"Kitten Rehabilitator",
			"Bunny Saver",
			"Red Panda Helper",
			"Love an Otter",
			"Sea Turtle Saviour",
			"Whale Guardian",
			"Red Panda Lord",
			"Ark Commandant"
		}, {
			"Locked",
			"Bounty Hunter",
			"Mercenary",
			"Soldier Of Fortune",
			"Hitman",
			"Assassin",
			"Black-Ops",
			"One Man Army",
			"Sliver Cell",
			"The One",
			"Chiroptera Hominin"
		}, {
			"Locked",
			"Gambler",
			"Slot Junkie",
			"Black Jacker",
			"Dice Master",
			"Pro Poker Player",
			"High Stakes Roller",
			"Lucky Duck",
			"Reign Man",
			"Sports Almanac",
			"God Of Gamblers"
		}, {
			"Locked",
			"Pro Athlete",
			"Puck Catcher",
			"Baseball Hitter",
			"Football Trower",
			"Face Puncher",
			"Endorsement Getter",
			"Team Buyer",
			"Franchise Haver",
			"Living Legend",
			"Hall Of Famer"
		}, {
			"Locked",
			"Paralegal",
			"Ambulance Chaser",
			"Lawyer",
			"State Attorney",
			"Judge Derp",
			"District Attorney",
			"Attorney General",
			"The Law",
			"Supreme Justice",
			"Avatar Of Order"
		}, {
			"Locked",
			"Actor",
			"Award Winning Actor",
			"Actor / Director",
			"Award Winning Director",
			"Auteur",
			"Cinematic Legend",
			"Phenomenon",
			"The Last Celebrity",
			"Literally A Star",
			"Cao Guojiu"
		}, {
			"Locked",
			"Astronaut",
			"Starship Captain",
			"Star Child",
			"Space Surveyor",
			"Star Searcher",
			"Infinite Explorer",
			"Interstellar Commandant",
			"Solar Emperor",
			"Galactic Overlord",
			"Power Cosmic"
		}, {
			"Locked",
			"Demon Hunter",
			"Vampire Slayer",
			"Dragon Killer",
			"Kracken Kracker",
			"Titan Tosser",
			"Planet Buster",
			"Star Destroy-Doer",
			"Deity Destroyer",
			"Chuck Slayer",
			"Everything Slayer"
		}, {
			"Locked",
			"Love Doctor",
			"Love Guru",
			"Love Fairy",
			"Love God",
			"Loveicus Prime",
			"Cupid",
			"Love Maker",
			"The Unity",
			"Eros",
			"Aphrodome"
		}, {
			"Locked",
			"Wizard",
			"Hairy Wizard",
			"Very Hairy Wizard",
			"Sorcerer",
			"More-cerer",
			"Magus Awesomus",
			"Grand Magus",
			"Archmage",
			"Arch Archmage",
			"Sorcerer Supremo"
		}, {
			"Locked",
			"Grave Digger",
			"Cemetary Dredger",
			"Burial Builder",
			"Cemetary Settler",
			"Crypt Keeper",
			"Mausoleum Maker",
			"Catacomb Crafter",
			"Sepulcher Scooper",
			"Ruin Revealer",
			"Tomb Exhumer"
		}, {
			"Locked",
			"Seed Sower",
			"Herb Helper",
			"Flora Fixer",
			"Grass Grower",
			"Potent Pollenator",
			"Shrub Setter",
			"Vine Vindicator",
			"Tree Transplanter",
			"Woodland Ward",
			"Forest Kami"
		}
	};
	private final static int[][][] values = new int[][][]{
		{
			{-1, 1, 0},
			{10, 5, 3},
			{12, 5, 3},
			{14, 5, 3},
			{16, 5, 3},
			{18, 5, 3},
			{20, 5, 3},
			{22, 5, 3},
			{24, 5, 3},
			{26, 5, 3},
			{30, 5, 2}
		}, {
			{-1, 1, 0},
			{24, 8, 4},
			{28, 8, 4},
			{32, 8, 4},
			{38, 8, 4},
			{44, 8, 4},
			{50, 8, 4},
			{58, 8, 4},
			{66, 8, 4},
			{74, 8, 4},
			{100, 8, 3}
		}, {
			{-1, 1, 0},
			{150, 30, 5},
			{200, 30, 5},
			{250, 30, 5},
			{300, 30, 5},
			{350, 30, 5},
			{400, 30, 5},
			{450, 30, 5},
			{500, 30, 5},
			{550, 30, 5},
			{600, 30, 4}
		}, {
			{-1, 1, 0},
			{9, 6, 2},
			{10, 6, 2},
			{11, 6, 2},
			{12, 6, 2},
			{14, 6, 2},
			{16, 6, 2},
			{18, 6, 2},
			{21, 6, 2},
			{24, 6, 2},
			{30, 6, 2}
		}, {
			{-1, 1, 0},
			{30, 60, 7},
			{60, 60, 7},
			{90, 60, 7},
			{120, 60, 7},
			{150, 60, 7},
			{180, 60, 7},
			{210, 60, 7},
			{240, 60, 7},
			{270, 60, 7},
			{15000, 60, 1}
		}, {
			{-1, 1, 0},
			{4200, 300, 8},
			{6600, 300, 8},
			{9000, 300, 8},
			{11400, 300, 8},
			{13800, 300, 8},
			{16200, 300, 8},
			{18600, 300, 8},
			{21000, 300, 8},
			{23400, 300, 8},
			{30000, 300, 6}
		}, {
			{-1, 1, 0},
			{48, 12, 6},
			{60, 12, 6},
			{72, 12, 6},
			{84, 12, 6},
			{96, 12, 6},
			{108, 12, 6},
			{120, 12, 6},
			{144, 12, 6},
			{168, 12, 6},
			{192, 12, 5}
		}, {
			{-1, 1, 0},
			{2000, 1000, 20},
			{10000, 1000, 18},
			{20000, 1000, 16},
			{40000, 1000, 14},
			{80000, 1000, 12},
			{120000, 1000, 10},
			{160000, 1000, 9},
			{200000, 1000, 8},
			{250000, 1000, 7},
			{300000, 1000, 6}
		}, {
			{-1, 1, 0},
			{600, 120, 2},
			{2400, 180, 2},
			{5400, 240, 2},
			{16200, 300, 2},
			{144000, 450, 1},
			{216000, 500, 1},
			{324000, 450, 1},
			{486000, 300, 1},
			{729000, 240, 1},
			{1093500, 180, 1}
		}, {
			{-1, 1, 0},
			{4500, 300, 15},
			{9000, 300, 13},
			{18000, 300, 11},
			{36000, 300, 9},
			{72000, 300, 7},
			{90000, 300, 6},
			{112500, 300, 5},
			{140625, 300, 4},
			{175781, 300, 3},
			{219727, 300, 2}
		}, {
			{-1, 1, 0},
			{240000, 600, 20},
			{350000, 600, 20},
			{450000, 600, 18},
			{600000, 600, 18},
			{800000, 600, 16},
			{1000000, 600, 16},
			{1200000, 600, 14},
			{1400000, 600, 14},
			{1600000, 600, 12},
			{1800000, 600, 12}
		}, {
			{-1, 1, 0},
			{1, 5, 20},
			{750, 5, 15},
			{1350, 5, 14},
			{2430, 5, 13},
			{4374, 5, 12},
			{7873, 5, 11},
			{14172, 5, 10},
			{25509, 5, 9},
			{45917, 5, 8},
			{82650, 5, 7}
		}, {
			{-1, 1, 0},
			{17280000, 8640, 25},
			{25920000, 8640, 25},
			{38880000, 8640, 25},
			{58320000, 8640, 20},
			{87480000, 8640, 20},
			{131220000, 8640, 20},
			{196830000, 8640, 15},
			{295245000, 8640, 15},
			{442867500, 8640, 15},
			{664301250, 8640, 10}
		}, {
			{-1, 1, 0},
			{50, 5, 10},
			{100, 5, 10},
			{200, 5, 10},
			{400, 5, 10},
			{800, 5, 10},
			{1600, 5, 10},
			{3200, 5, 10},
			{6400, 5, 10},
			{12800, 5, 10},
			{25600, 5, 10}
		}, {
			{-1, 1, 0},
			{9000, 14, 25},
			{11250, 13, 25},
			{14062, 12, 20},
			{17578, 11, 15},
			{21972, 10, 10},
			{27465, 9, 9},
			{34332, 8, 8},
			{42915, 7, 7},
			{53644, 6, 6},
			{67055, 5, 5}
		}, {
			{-1, 1, 0},
			{1, 315360, 25},
			{25, 630720, 25},
			{625, 1261440, 25},
			{15625, 2522880, 25},
			{390625, 5045760, 25},
			{9765625, 10091520, 25},
			{244140625, 20183040, 20},
			{610351562, 4036608, 15},
			{152587890, 80732, 10},
			{381469726, 16146, 5}
		}, {
			{-1, 1, 0},
			{1, 66, 6},
			{6, 66, 6},
			{36, 66, 6},
			{216, 66, 6},
			{1296, 66, 6},
			{7776, 66, 6},
			{46656, 66, 6},
			{279936, 66, 6},
			{1679616, 66, 6},
			{10077696, 66, 6}
		}, {
			{-1, 1, 0},
			{55555, 333, 35},
			{99999, 333, 33},
			{189998, 333, 33},
			{379996, 333, 30},
			{797992, 333, 30},
			{1755582, 333, 27},
			{4037840, 333, 27},
			{9690815, 333, 23},
			{24227038, 333, 23},
			{62990298, 333, 20}
		}
	};
}