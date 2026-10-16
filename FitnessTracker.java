
import java.util.*;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.stream.*;

// Module 4: Interface
interface Trackable {
    double calories();
}

// Module 4: Class and object
class Workout implements Comparable<Workout>, Trackable {
    private int id, duration;
    private String activity, intensity;
    private double weight;
    private LocalDate date;

    static int nextId = 1;
    static final int GOAL = 150;

    // Default constructor
    Workout() {
        this("Walking", 30, 60, "Medium");
    }

    // Parameterized constructor
    Workout(String a, int d, double w, String i) {
        id = nextId++;
        activity = a;
        duration = d;
        weight = w;
        intensity = i;
        date = LocalDate.now();
    }

    void setId(int id) {
    this.id = id;
}

void setDate(LocalDate date) {
    this.date = date;
}

    // Accessors and mutator
    int getId() { return id; }
    int getDuration() { return duration; }
    String getActivity() { return activity; }

    void setDuration(int d) {
        if (d <= 0)
            throw new IllegalArgumentException("Invalid duration");
        duration = d;
    }

    @Override
    public double calories() {
        double met = intensity.equalsIgnoreCase("High") ? 8 :
                     intensity.equalsIgnoreCase("Low") ? 2.5 : 5;
        return met * 3.5 * weight * duration / 200;
    }

    @Override
    public int compareTo(Workout w) {
        return Integer.compare(duration, w.duration);
    }

    @Override
    public String toString() {
        return String.format(
            "%d | %s | %s | %d min | %.1f calories",
            id, date, activity, duration, calories());
    }

    String toFile() {
        return id + "|" + date + "|" + activity + "|" +
               duration + "|" + weight + "|" + intensity;
    }
}

public class FitnessTracker {
    static Scanner sc = new Scanner(System.in);
    static ArrayList<Workout> workouts = new ArrayList<>();
    static HashSet<String> activities = new HashSet<>();
    static HashMap<String, Integer> counts = new HashMap<>();
    static Path file = Paths.get("workouts.txt");

    public static void main(String[] args) {
        load();
        int choice;
        do {
            menu();
            choice = number("Choice: ");
            switch (choice) {
                case 1: add(); break;
                case 2: view(); break;
                case 3: search(); break;
                case 4: update(); break;
                case 5: delete(); break;
                case 6: summary(); break;
                case 7: bmi(); break;
                case 8: sort(); break;
                case 9: streamReport(); break;
                case 10: weeklyReport(); break;
                case 11: save(); break;
                case 0: save(); System.out.println("Goodbye!"); break;
                default: System.out.println("Invalid choice.");
            }
        } while (choice != 0);
    }

    static void menu() {
        System.out.println("\n=== FITNESS TRACKER ===");
        System.out.println("1.Add  2.View  3.Search  4.Update");
        System.out.println("5.Delete  6.Summary  7.BMI  8.Sort");
        System.out.println("9.Stream report  10.Weekly report");
        System.out.println("11.Save  0.Exit");
    }

    // Module 1: Console input and exception handling
    static int number(String msg) {
        while (true) {
            try {
                System.out.print(msg);
                return Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Enter a valid integer.");
            }
        }
    }

    static double decimal(String msg) {
        while (true) {
            try {
                System.out.print(msg);
                double n = Double.parseDouble(sc.nextLine().trim());
                if (Double.isFinite(n)) return n;
            } catch (NumberFormatException e) {
                System.out.println("Enter a valid number.");
            }
            System.out.println("Invalid input.");
        }
    }

    static String text(String msg) {
        System.out.print(msg);
        return sc.nextLine().trim();
    }

    // Module 2: Selection and validation
    static void add() {
        String a = text("Activity: ").replace("|", "/");
        int d = number("Duration (minutes): ");
        double w = decimal("Weight (kg): ");
        String i = text("Intensity (Low/Medium/High): ");

        if (a.isEmpty() || d <= 0 || w <= 0) {
            System.out.println("Invalid workout details.");
            return;
        }
        if (!(i.equalsIgnoreCase("Low") ||
              i.equalsIgnoreCase("Medium") ||
              i.equalsIgnoreCase("High"))) {
            System.out.println("Invalid intensity.");
            return;
        }

        workouts.add(new Workout(a, d, w, i));
        refresh();
        System.out.println("Workout added.");
    }

    static void view() {
        if (workouts.isEmpty()) {
            System.out.println("No workouts found.");
            return;
        }
        for (Workout w : workouts)
            System.out.println(w);
    }

    static Workout find(int id) {
        for (Workout w : workouts)
            if (w.getId() == id) return w;
        return null;
    }

    static void search() {
        Workout w = find(number("Workout ID: "));
        System.out.println(w == null ? "Not found." : w);
    }

    static void update() {
        Workout w = find(number("Workout ID: "));
        if (w == null) {
            System.out.println("Not found.");
            return;
        }
        try {
            w.setDuration(number("New duration: "));
            System.out.println("Updated.");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
        refresh();
    }

    static void delete() {
        Workout w = find(number("Workout ID: "));
        if (w != null) {
            workouts.remove(w);
            refresh();
            System.out.println("Deleted.");
        } else System.out.println("Not found.");
    }

    // Module 3: Summation, average and recursion
    static void summary() {
        int minutes = 0;
        double calories = 0;
        for (Workout w : workouts) {
            minutes += w.getDuration();
            calories += w.calories();
        }

        double avg = workouts.isEmpty() ? 0 :
                     (double) minutes / workouts.size();

        System.out.println("Workouts: " + workouts.size());
        System.out.println("Total minutes: " + minutes);
        System.out.printf("Average duration: %.1f%n", avg);
        System.out.printf("Calories burned: %.1f%n", calories);
        System.out.println("Goal remaining: " +
                           Math.max(0, Workout.GOAL - minutes));
        System.out.printf("Recursive calorie total: %.1f%n",
                          recursive(0));
    }

    static double recursive(int index) {
        if (index >= workouts.size()) return 0;
        return workouts.get(index).calories() + recursive(index + 1);
    }

    static void bmi() {
        double w = decimal("Weight (kg): ");
        double h = decimal("Height (metres): ");
        if (w <= 0 || h <= 0) {
            System.out.println("Values must be positive.");
            return;
        }
        double bmi = w / (h * h);
        System.out.printf("BMI: %.2f%n", bmi);
        String result = bmi < 18.5 ? "Underweight" :
                        bmi < 25 ? "Normal range" :
                        bmi < 30 ? "Overweight" : "Obesity range";
        System.out.println(result);
    }

    // Module 5: Sorting, searching and StringBuilder
    static void sort() {
        Collections.sort(workouts);
        view();

        String[] names = workouts.stream()
            .map(Workout::getActivity).toArray(String[]::new);
        Arrays.sort(names, String.CASE_INSENSITIVE_ORDER);
        System.out.println("Sorted activities: " +
                           Arrays.toString(names));

        String target = text("Search activity: ");
        int index = Arrays.binarySearch(names, target,
                                      String.CASE_INSENSITIVE_ORDER);
        System.out.println(index >= 0 ? "Found." : "Not found.");

        StringBuilder sb = new StringBuilder("Activities: ");
        for (String name : names) sb.append(name).append(" ");
        System.out.println(sb);

        StringBuffer buffer = new StringBuffer("Stay ");
        buffer.append("Fit!");
        System.out.println(buffer);
    }

    // Module 6: Collections and streams
    static void refresh() {
        activities.clear();
        counts.clear();
        for (Workout w : workouts) {
            activities.add(w.getActivity());
            String key = w.getActivity().toLowerCase();
            counts.put(key, counts.getOrDefault(key, 0) + 1);
        }
    }

    static void streamReport() {
        refresh();
        System.out.println("Unique activities: " + activities);
        System.out.println("Activity counts: " + counts);

        workouts.stream()
            .filter(w -> w.getDuration() >= 30)
            .forEach(System.out::println);

        long count = workouts.stream()
            .filter(w -> w.getDuration() >= 30).count();
        double total = workouts.stream()
            .map(Workout::calories)
            .reduce(0.0, Double::sum);

        List<String> names = workouts.stream()
            .map(Workout::getActivity)
            .distinct().sorted()
            .collect(Collectors.toList());

        String report = names.stream()
            .collect(Collectors.joining(", "));

        System.out.println("Workouts >= 30 min: " + count);
        System.out.printf("Total calories: %.1f%n", total);
        System.out.println("Activities: " + report);
    }

    // Module 3: 2D array and nested loops
    static void weeklyReport() {
        int[][] data = new int[7][2];
        LocalDate today = LocalDate.now();

        for (Workout w : workouts) {
            long days = java.time.temporal.ChronoUnit.DAYS
                .between(w.date, today);
            if (days >= 0 && days < 7) {
                int row = 6 - (int) days;
                data[row][0] += w.getDuration();
                data[row][1] += (int) w.calories();
            }
        }

        for (int r = 0; r < 7; r++) {
            System.out.print(today.minusDays(6 - r) + ": ");
            for (int c = 0; c < 2; c++)
                System.out.print(data[r][c] + " ");
            System.out.println();
        }
    }

    // Module 5: File input/output and try-with-resources
    static void save() {
        try (BufferedWriter bw = Files.newBufferedWriter(file)) {
            for (Workout w : workouts) {
                bw.write(w.toFile());
                bw.newLine();
            }
            System.out.println("Data saved.");
        } catch (IOException | SecurityException e) {
            System.out.println("Save error: " + e.getMessage());
        }
    }

    
static void load() {
    if (!Files.exists(file))
        return;

    try (BufferedReader br = Files.newBufferedReader(file)) {
        String line;

        while ((line = br.readLine()) != null) {
            try {
                String[] p = line.split("\\|", -1);

                if (p.length != 6)
                    continue;

                int id = Integer.parseInt(p[0]);
                LocalDate date = LocalDate.parse(p[1]);
                String activity = p[2];
                int duration = Integer.parseInt(p[3]);
                double weight = Double.parseDouble(p[4]);
                String intensity = p[5];

                if (id <= 0 || duration <= 0 ||
                    !Double.isFinite(weight) || weight <= 0 ||
                    activity.isEmpty() || intensity.isEmpty())
                    continue;

                // Use the parameterized constructor
                Workout w = new Workout(
                    activity, duration, weight, intensity);

                // Restore the saved ID and date
                w.setId(id);
                w.setDate(date);

                workouts.add(w);
                Workout.nextId = Math.max(Workout.nextId, id + 1);

            } catch (RuntimeException e) {
                System.out.println("Skipping invalid record: " + line);
            }
        }

        refresh();

    } catch (IOException | SecurityException e) {
        System.out.println("Load error: " + e.getMessage());
    }
}
}