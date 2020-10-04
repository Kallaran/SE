
/* AirSimulation class
 *
 * TP of SE (version 2020)
 *
 * AM
 */

import java.util.Random;
import java.util.ArrayList;

public class AirSimulation {
	private int nAgent1;
	private int nAgent2;
	private int nAgent3;
	private int nAgent4;
	private Aircraft a;
	public final int nagents = 4;

	// Constructor
	public AirSimulation() {
		this.nAgent1 = 0;
		this.nAgent2 = 0;
		this.nAgent3 = 0;
		this.nAgent4 = 0;
		this.a = new Aircraft(); // standard model
	}

	// Reference to Aircraft
	public Aircraft getAircraftRef() {
		return this.a;
	}

	// Agent1
	public void agent1() throws InterruptedException {
		boolean placed = false;
		Random R = new Random();
		ArrayList<Integer> emergRows = this.a.getEmergencyRowList();

		// generating a new Customer
		Customer c = new Customer();

		// randomly pick a seat
		do {
			int row = R.nextInt(this.a.getNumberOfRows());
			int col = R.nextInt(this.a.getSeatsPerRow());

			// verifying whether the seat is free
			if (this.a.isSeatEmpty(row, col)) {
				// if this is an emergency exit seat, and c is over60, then we skip
				if (!emergRows.contains(row) || !c.isOver60()
						|| this.a.numberOfFreeSeats() <= this.a.getSeatsPerRow() * this.a.getNumberEmergencyRows()) {
					this.a.add(c, row, col);
					placed = true;
				}
			}
		} while (!placed && !this.a.isFlightFull());

		// updating counter
		if (placed)
			this.nAgent1++;
	}

	// Agent2
	public void agent2() throws InterruptedException {
		boolean placed = false;
		ArrayList<Integer> emergRows = this.a.getEmergencyRowList();

		// generating a new Customer
		Customer c = new Customer();

		// searching free seats on the seatMap
		int row = 0;
		while (!placed && !this.a.isFlightFull() && row < this.a.getNumberOfRows()) {
			int col = 0;
			while (!placed && col < this.a.getSeatsPerRow()) {
				// verifying whether the seat is free
				if (this.a.isSeatEmpty(row, col)) {
					// if this is an emergency exit seat, and c needs assistence, then we skip
					if (!emergRows.contains(row) || !c.needsAssistence() || this.a
							.numberOfFreeSeats() <= this.a.getSeatsPerRow() * this.a.getNumberEmergencyRows()) {
						this.a.add(c, row, col);
						placed = true;
					}
				}
				col++;
			}
			row++;
		}

		// updating counter
		if (placed)
			this.nAgent2++;
	}

	// Agent3
	public void agent3() throws InterruptedException {
		Random R = new Random();

		int row1 = 0;
		int col1 = 0;
		int row2 = 0;
		int col2 = 0;

		// randomly pick 2 seats
		do {
			row1 = R.nextInt(this.a.getNumberOfRows());
			col1 = R.nextInt(this.a.getSeatsPerRow());
			row2 = R.nextInt(this.a.getNumberOfRows());
			col2 = R.nextInt(this.a.getSeatsPerRow());
		} while (this.a.isSeatEmpty(row1, col1) || this.a.isSeatEmpty(row2, col2));

		// get customers from these seats
		Customer c1 = this.a.getCustomer(row1, col1);
		Customer c2 = this.a.getCustomer(row2, col2);

		// get c1 and c2 flyer level
		int C1freq = c1.getFlyerLevel();
		int C2freq = c2.getFlyerLevel();

		// if c1 is in front of c2 but his frequentFlyer is lower than c2's
		// frequentFlyer then switch
		if ((row1 < row2) && (C1freq < C2freq)) {

			// free these seats
			this.a.freeSeat(row1, col1);
			this.a.freeSeat(row2, col2);

			// switch the seats of the two customers
			this.a.add(c2, row1, col1);
			this.a.add(c1, row2, col2);
		}

		this.nAgent3++;
	}

	// Agent4: the virus
	public void agent4() throws InterruptedException {
		for (int i = 0; i < this.a.getNumberOfRows(); i++) {
			for (int j = 0; j < this.a.getSeatsPerRow(); j++) {
				Customer c = this.a.getCustomer(i, j);
				this.a.freeSeat(i, j);
				if (c != null)
					this.a.add(c, i, j);
			}
		}
		this.nAgent4++;
	}

	// Resetting
	public void reset() {
		this.nAgent1 = 0;
		this.nAgent2 = 0;
		this.nAgent3 = 0;
		this.nAgent4 = 0;
		this.a.reset();
	}

	// Printing
	public String toString() {
		String print = "AirSimulation (agent1 : " + this.nAgent1 + ", agent2 : " + this.nAgent2 + ", " + "agent3 : "
				+ this.nAgent3 + ", agent4 : " + this.nAgent4 + ")\n";
		print = print + a.toString();
		return print;
	}

	class ThreadAgent extends Thread {
		private Thread t;
		private String threadName;
		AirSimulation s;

		ThreadAgent(String name, AirSimulation s) {
			threadName = name;
			this.s = s;
		}

		public void run() {
			try {
				switch (threadName) {
				case "Thread-agent2":
					while (!s.getAircraftRef().isFlightFull()) {
						s.agent2();
					}
					break;
				case "Thread-agent3":
					while (!s.getAircraftRef().isFlightFull()) {
						s.agent3();
					}
					break;
				case "Thread-agent4":
					while (!s.getAircraftRef().isFlightFull()) {
						s.agent4();
					}
					break;
				default:
					System.out.println("no match");
				}
			} catch (InterruptedException e) {
				System.out.println("Thread " + threadName + " interrupted.");
			}
		}

		public void start() {
			if (t == null) {
				t = new Thread(this, threadName);
				t.start();
			}
		}
	}

	// Simulation in sequential (main)
	public static void main(String[] args) throws InterruptedException {

		long begin = System.currentTimeMillis();

		// System.out.println("\n** Sequential execution **\n");
		if (args != null && args.length > 0 && args[0] != null && args[0].equals("animation")) {
			AirSimulation s = new AirSimulation();
			ThreadAgent T2 = s.new ThreadAgent("Thread-agent2", s);
			ThreadAgent T3 = s.new ThreadAgent("Thread-agent3", s);
			ThreadAgent T4 = s.new ThreadAgent("Thread-agent4", s);
			T2.start();
			T3.start();
			T4.start();
			while (!s.a.isFlightFull()) {
				s.agent1();
				System.out.println(s + s.a.cleanString());
				Thread.sleep(100);
			}
			System.out.println(s);
		} else {
			AirSimulation s = new AirSimulation();
			ThreadAgent T2 = s.new ThreadAgent("Thread-agent2", s);
			ThreadAgent T3 = s.new ThreadAgent("Thread-agent3", s);
			ThreadAgent T4 = s.new ThreadAgent("Thread-agent4", s);
			T2.start();
			T3.start();
			T4.start();
			while (!s.a.isFlightFull()) {
				s.agent1();
			}
			System.out.println(s);
		}
		long end = System.currentTimeMillis();
		long time = end - begin;
		System.out.println(time);
	}
}
