package it.twopay.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.twopay.rules.StatelessRunner;

public class Car {
	public static Logger log = LoggerFactory.getLogger("it.twopay.entity.Car");
	
	public enum Actions {
		CHECK_MIRROR("CHECK_MIRROR"),
		RELEASE_PARKING_BRAKE("RELEASE_PARKING_BRAKE"),
		FASTEN_SEATBELT("FASTEN_SEATBELT"),
		FILL_UP_TANK("FILL_UP_TANK"),
		IGNITION("IGNITION"),
		START_TRIP("START_TRIP"),
		END_TRIP("END_STRIP"),
		PARK("PARK"),
		TURN_OFF("TURN_OFF"),
		APPLY_PARKING_BRAKE("APPLY_PARKING_BRAKE"),
		RELEASE_SEATBELT("RELEASE_SEATBELT");
		private String action;
		private Actions(String action) {
			this.action = action;
		}
		public String getAction() {
			return action;
		}
		@Override 
        public String toString(){ 
            return action; 
        }
	}
	
	public enum Gears {
		GEAR_1(1), GEAR_2(2), GEAR_3(3),
		GEAR_4(4), GEAR_5(5), GEAR_6(6);
		private Integer gear;
		private Gears(Integer gear) {
			this.gear = gear;
		}
		public int getValue() {
			return gear;
		}
		public Gears getByValue(int gear) {
			Gears result = null;
			switch (gear) {
			case 1:
				result = GEAR_1;
				break;
			case 2:
				result = GEAR_2;
				break;
			case 3:
				result = GEAR_3;
				break;
			case 4:
				result = GEAR_4;
				break;
			case 5:
				result = GEAR_5;
				break;
			case 6:
				result = GEAR_6;
				break;
			}
			return result;
		}
		@Override 
        public String toString(){ 
            return "GEAR_" + gear; 
        }
	}
	
	private static final double ACCELERATION_SEED = 500;
	
	private Map<Gears, Double> gearRatio;
	private List<Actions> actions;
	private List<Actions> delayedActions;
	
	private Gears gear = null;
	private boolean running = false;
	private double rpm = 0;
	private String state = "init";
	private int stateCount = 0;

	private void setup_gear_ratio() {
		gearRatio = new HashMap<>();
		
		// Ratio to convert RPM to KM/H
		gearRatio.put(Gears.GEAR_1, 94.25);
		gearRatio.put(Gears.GEAR_2, 63.0625);
		gearRatio.put(Gears.GEAR_3, 46.0625);
		gearRatio.put(Gears.GEAR_4, 35.4375);
		gearRatio.put(Gears.GEAR_5, 26.1875);
		gearRatio.put(Gears.GEAR_6, 17.6875);
	}

	public Car() {
		setup_gear_ratio();
		actions = new ArrayList<>();
	}
	
	public void accelerate() {
		if (running) {
			rpm += ACCELERATION_SEED * Math.random();
			log.debug("Accelerated to rpm: " + rpm);
		}
	}
	
	public void perform(Actions action) {
		switch(action) {
		case IGNITION:
			setRunning(true);
			break;
		case TURN_OFF:
			// TURN_OFF should include action from END_TRIP
			setRunning(false);
		case END_TRIP:
			// Pretty abrupt stop, I know...
			rpm = 1;
			gear = null;
			break;
		default:
			break;
		}
		addAction(action);
	}
	
	public void upShift() {
		if (running) {
			if (gear == null) {
				gear = Gears.GEAR_1;
			} else {
				int currentGear = gear.getValue();
				if (currentGear < Gears.GEAR_6.getValue()) {
					gear = gear.getByValue(++currentGear);
					log.debug("Up shit to gear " + gear);
				}
			}
		}
	}
	
	public void downShift() {
		if (running && gear != null) {
			int currentGear = gear.getValue();
			if (currentGear > Gears.GEAR_1.getValue()) {
				gear = gear.getByValue(--currentGear);
				log.debug("Down shit to gear " + gear);
			} else if (currentGear == Gears.GEAR_1.getValue()) {
				gear = null;
			}
		}
	}
	
	public void process() {
		StatelessRunner runner = new StatelessRunner("CarKS");
		runner.insert(this);
		runner.process();
	}
	
	public void addDelayAction(Actions action) {
		// Action to be performed after END_STRIP
		delayedActions.add(action);
	}
	
	// Property Methods
	public double getSpeed(Gears gear, double rpm) {
		if (gear == null) {
			return 0.0;
		} else {
			double ratio = gearRatio.get(gear);
			return rpm / ratio;
		}
	}
	public double getSpeed() {
		return getSpeed(gear, rpm);
	}
	private void addAction(Actions action) {
		actions.add(action);
	}


	
	// Properties
	private void setRunning(boolean running) {
		if (running != this.running) {
			this.running = running;
			gear = null; // Gear always start and end as null
			if (running) {
				rpm = 1;
			} else {
				rpm = 0;
			}
		}
	}
	
	public boolean isRunning() {
		return running;
	}

	public double getRpm() {
		return rpm;
	}

	public void setRpm(double rpm) {
		this.rpm = rpm;
	}

	public Gears getGear() {
		return gear;
	}
	
	public List<Actions> getActions() {
		return actions;
	}

	public List<Actions> getEndActions() {
		return delayedActions;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		if (state == this.state) {
			stateCount++;
		} else {
			stateCount = 1;
		}
		this.state = state;
	}

	public int getStateCount() {
		return stateCount;
	}

	
	
}
