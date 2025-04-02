import java.util.*;

public class SolutionWithoutMatriz {
    public static int POPULATION_SIZE = 1500;
    public static int ROUNDS_WITHOUT_CHANGE = 100;
    public static double CROSSOVER_RATE = 0.17;
    public static double MUTATION_RATE = 0.1;

    public static void main(String[] args) {
        final Random random = new Random();

        Scanner scanner = new Scanner(System.in);
        System.out.println("Digite as cidades solicitadas separadas por espaço (ex.: 6 0 3):");
        String input = scanner.nextLine();
        int[] citiesRequested = parseCities(input);

        
        if (citiesRequested.length == 0) {
            System.out.println("Deve solicitar pelo menos uma cidade");
            return;
        }

        int[][] cities = FileService.readInputFile("cidades.txt");

        if (cities == null) {
            System.out.println("Erro ao abrir arquivo");
            return;
        }


        if (cities.length < citiesRequested.length) {
            System.out.println("Mais cidades solicitadas do que as disponíveis");
            return;
        }

        ArrayList<ArrayList<Integer>> population = new ArrayList<>();
        for (int i = 0; i < POPULATION_SIZE; i++) {
            population.add(createRandomRoute(cities.length, citiesRequested, random));
        }

        int rounds = 0;
        Feedback best = null;
        while (rounds < ROUNDS_WITHOUT_CHANGE) {

            ArrayList<Feedback> validPopulation = new ArrayList<>();
            Feedback bestOfPopulation = null;
            int bestDistance = Integer.MAX_VALUE;

            for (List<Integer> route : population) {
                Feedback feedback = isValidSolution(route, cities, citiesRequested);

                if (feedback.isValid) {
                    System.out.println("Valid: " + route);
                    validPopulation.add(feedback);
                    if (feedback.distance < bestDistance) {
                        bestDistance = feedback.distance;
                        bestOfPopulation = feedback;
                    }
                }
            }

            if (!validPopulation.isEmpty() && bestOfPopulation != null) {
                if (best == null) {
                    best = bestOfPopulation;
                } else if (bestOfPopulation.distance < best.distance) {
                    best = bestOfPopulation;
                    rounds = 0;
                } else {
                    rounds++;
                }
            }

            ArrayList<ArrayList<Integer>> newPopulation = new ArrayList<>();

            for (int i = 0; i < POPULATION_SIZE; i++) {
                if (validPopulation.size() >= 2) {
                    ArrayList<Integer> father1 = getFather(validPopulation, random).route;
                    ArrayList<Integer> father2 = getFather(validPopulation, random).route;
                    ArrayList<Integer> child;

                    if (random.nextDouble() <= CROSSOVER_RATE) {
                        child = crossover(father1, father2, random);
                    } else {
                        child = new ArrayList<>(getBest(validPopulation.get(0), validPopulation.get(1)).route);
                    }

                    mutation(child, random);
                    newPopulation.add(child);

                } else {
                    System.out.printf("População irá morrer, apenas %d sobreviventes.", validPopulation.size());
                    return;
                }
            }

            population = newPopulation;
        }
        System.out.println("----- END JOB -----");
        System.out.println("Fim: " + best.route + " | " + best.distance);

    }

    private static ArrayList<Integer> createRandomRoute(int cityCount, int[] citiesRequested, Random random) {
        int minRouteLength = citiesRequested.length;
        int routeLength = minRouteLength + random.nextInt(cityCount - minRouteLength + 1);
        ArrayList<Integer> availableCities = new ArrayList<>();
        for (int i = 0; i < cityCount; i++) {
            availableCities.add(i);
        }

        int startCity = citiesRequested[0];
        Collections.shuffle(availableCities, random);

        ArrayList<Integer> route = new ArrayList<>();
        route.add(startCity);
        for (int i = 0; i < routeLength - 1; i++) {
            route.add(availableCities.get(i));
        }
        return route;
    }

    private static Feedback isValidSolution(List<Integer> route, int[][] cities, int[] citiesRequested) {
        Feedback feedback = new Feedback(route);

        if (route.isEmpty() || !route.getFirst().equals(citiesRequested[0])) {
            return feedback;
        }

        if (!route.getFirst().equals(route.getLast())) {
            return feedback;
        }

        // Verifica se todas as cidades solicitadas estão presentes na rota
        for (int city : citiesRequested) {
            if (!route.contains(city)) {
                return feedback;
            }
        }

        java.util.HashSet<Integer> uniqueCities = new java.util.HashSet<>();
        for (int i = 0; i < route.size() - 1; i++) {
            int city = route.get(i);
            if (uniqueCities.contains(city)) {
                return feedback;
            }
            uniqueCities.add(city);
        }

        int totalDistance = 0;
        for (int i = 0; i < route.size() - 1; i++) {
            totalDistance += cities[route.get(i)][route.get(i + 1)];
        }

        feedback.distance = totalDistance;
        feedback.isValid = true;
        return feedback;
    }

    private static ArrayList<Integer> crossover(ArrayList<Integer> father1, ArrayList<Integer> father2, Random random) {
        ArrayList<Integer> larger, smaller;

        if (father1.size() >= father2.size()) {
            larger = new ArrayList<>(father1);
            smaller = father2;
        } else {
            larger = new ArrayList<>(father2);
            smaller = father1;
        }

        int sizeSmaller = smaller.size();
        int start = random.nextInt(sizeSmaller);
        int end = random.nextInt(sizeSmaller);
        if (start > end) {
            int temp = start;
            start = end;
            end = temp;
        }

        for (int i = start; i <= end; i++) {
            larger.set(i, smaller.get(i));
        }

        return larger;
    }

    private static void mutation(ArrayList<Integer> route, Random random) {
        for (int i = 0; i < route.size(); i++) {
            if (random.nextDouble() <= MUTATION_RATE) {
                int j = random.nextInt(route.size());
                route.remove(j);
            }
        }
    }

    private static Feedback getFather(ArrayList<Feedback> population, Random random) {
        Feedback candidate1 = population.get(random.nextInt(population.size()));
        Feedback candidate2 = population.get(random.nextInt(population.size()));
        return getBest(candidate1, candidate2);
    }

    private static Feedback getBest(Feedback p1, Feedback p2) {
        return p1.distance <= p2.distance ? p1 : p2;
    }

    private static int[] parseCities(String input) {
        String[] parts = input.trim().split("\\s+");
        int[] cities = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            cities[i] = Integer.parseInt(parts[i]);
        }
        return cities;
    }
}

class Feedback {
    public int distance;
    public boolean isValid;
    public ArrayList<Integer> route;

    public Feedback(List<Integer> route) {
        this.route = new ArrayList<>(route);
        this.isValid = false;
        this.distance = 0;
    }
}