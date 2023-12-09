import java.util.*;

class Graph {
    private int vertices;
    private Map<Integer, Map<Integer, Integer>> adjacencyList;

    public Graph(int vertices) {
        this.vertices = vertices;
        this.adjacencyList = new HashMap<>();
        for (int i = 1; i <= vertices; i++) {
            this.adjacencyList.put(i, new HashMap<>());
        }
    }

    public void addEdge(int source, int destination, int weight) {
        this.adjacencyList.get(source).put(destination, weight);
        this.adjacencyList.get(destination).put(source, weight); // For undirected graph
    }

    public Map<Integer, Integer> dijkstra(int source) {
        PriorityQueue<Node> priorityQueue = new PriorityQueue<>(Comparator.comparingInt(node -> node.distance));
        Map<Integer, Integer> distance = new HashMap<>();
        Set<Integer> visited = new HashSet<>();

        for (int i = 1; i <= vertices; i++) {
            distance.put(i, Integer.MAX_VALUE);
        }

        distance.put(source, 0);
        priorityQueue.add(new Node(source, 0));

        while (!priorityQueue.isEmpty()) {
            int current = priorityQueue.poll().vertex;
            if (visited.contains(current)) {
                continue;
            }

            visited.add(current);

            for (Map.Entry<Integer, Integer> neighbor : adjacencyList.get(current).entrySet()) {
                int next = neighbor.getKey();
                int newDistance = distance.get(current) + neighbor.getValue();

                if (newDistance < distance.get(next)) {
                    distance.put(next, newDistance);
                    priorityQueue.add(new Node(next, newDistance));
                }
            }
        }

        return distance;
    }

    static class Node {
        int vertex;
        int distance;

        public Node(int vertex, int distance) {
            this.vertex = vertex;
            this.distance = distance;
        }
    }
}

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Masukkan kapasitas maksimal truk: ");
        int maxTruckCapacity = scanner.nextInt();

        int disposalSite = 5; // Tempat pembuangan sampah
        int numCities = 4; // Jumlah kota

        Graph graph = new Graph(numCities + 1); // Add 1 for disposal site

        int[][] distances = {
                {0, 10, 15, 20, 25},  // Kota 1 ke Kota 1, 2, 3, 4, dan Tempat Pembuangan
                {10, 0, 35, 25, 30},  // Kota 2 ke Kota 1, 2, 3, 4, dan Tempat Pembuangan
                {15, 35, 0, 30, 40},  // Kota 3 ke Kota 1, 2, 3, 4, dan Tempat Pembuangan
                {20, 25, 30, 0, 35},  // Kota 4 ke Kota 1, 2, 3, 4, dan Tempat Pembuangan
                {25, 30, 40, 35, 0}   // Tempat Pembuangan ke Kota 1, 2, 3, 4, dan Tempat Pembuangan
        };

        // Inisialisasi peta jarak antar kota
        for (int i = 1; i <= numCities + 1; i++) {
            for (int j = 1; j <= numCities + 1; j++) {
                if (i != j) {
                    graph.addEdge(i, j, distances[i - 1][j - 1]);
                }
            }
        }

        int currentTruckCapacity = 0;

        Map<Integer, Integer> garbageAmounts = new HashMap<>();
        for (int i = 1; i <= numCities; i++) {
            System.out.print("Masukkan jumlah sampah di kota " + i + ": ");
            int garbage = scanner.nextInt();
            garbageAmounts.put(i, garbage);
        }

        Map<Integer, Integer> distance = graph.dijkstra(disposalSite); // Assuming start from disposal site

        List<Integer> truckRoute = new ArrayList<>();
        int totalDistance = 0;
        int currentCity = disposalSite;

        System.out.println("\n==========================================" + 
                             "\nUrutan jalan truk dalam mengangkut sampah:");

        while (garbageAmounts.values().stream().anyMatch(amount -> amount > 0)) {
            int nextCity = findNextCity(garbageAmounts, distance, maxTruckCapacity, currentCity);

            // Kembali ke tempat pembuangan sampah setelah kapasitas truk terpenuhi atau semua kota sudah bersih
            if (nextCity == -1 || garbageAmounts.values().stream().allMatch(amount -> amount == 0)) {
                System.out.println("\nTruk kembali ke tempat pembuangan sampah untuk mengosongkan sampah");
                currentTruckCapacity = 0; // Reset kapasitas truk
                nextCity = disposalSite; // Kembali ke tempat pembuangan sampah

                // Mengangkut sisa sampah yang masih ada di kota sampai semua sampah habis
                for (int i = 1; i <= numCities; i++) {
                    if (garbageAmounts.get(i) > 0) {
                        int garbageCollected = Math.min(maxTruckCapacity, garbageAmounts.get(i));
                        garbageAmounts.put(i, garbageAmounts.get(i) - garbageCollected);
                        currentTruckCapacity += garbageCollected;

                        truckRoute.add(i);
                        totalDistance += distance.get(currentCity);

                        System.out.println("\nTruk mengunjungi kota " + i + "\nTruk mengangkut sampah sebanyak " + garbageCollected);
                        System.out.println("Jumlah sampah di kota " + i + " sekarang: " + garbageAmounts.get(i));

                        // Pindah ke kota berikutnya
                        currentCity = i;
                    }
                }
            } else {
                System.out.println("\nTruk mengunjungi kota " + nextCity);

                // Menghitung sampah yang diangkut oleh truk
                int garbageCollected = Math.min(maxTruckCapacity - currentTruckCapacity, garbageAmounts.get(nextCity));
                garbageAmounts.put(nextCity, garbageAmounts.get(nextCity) - garbageCollected);

                // Menambah kapasitas truk sesuai dengan sampah yang diangkut
                currentTruckCapacity += garbageCollected;

                truckRoute.add(nextCity);
                totalDistance += distance.get(currentCity);

                System.out.println("Truk mengangkut sampah sebanyak " + garbageCollected);
                System.out.println("Jumlah sampah di kota " + nextCity + " sekarang: " + garbageAmounts.get(nextCity));

                currentCity = nextCity;
            }
        }

        System.out.println("\nTotal jarak: " + totalDistance);
        System.out.println("Urutan kota yang dilalui: " + truckRoute);
    }

    private static int findNextCity(Map<Integer, Integer> garbageAmounts, Map<Integer, Integer> distance, int maxTruckCapacity, int currentCity) {
        int minDistance = Integer.MAX_VALUE;
        int nextCity = -1;

        for (Map.Entry<Integer, Integer> entry : garbageAmounts.entrySet()) {
            int city = entry.getKey();
            int cityDistance = distance.get(city);

            if (cityDistance < minDistance && cityDistance <= maxTruckCapacity && entry.getValue() > 0 && city != currentCity) {
                minDistance = cityDistance;
                nextCity = city;
            }
        }

        return nextCity;
    }
}
