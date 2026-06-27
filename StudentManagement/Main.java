import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        StudentService studentService = new StudentService();
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("\n=== Student Management System ===");
            System.out.println("1. Add Student");
            System.out.println("2. View All Students");
            System.out.println("3. Search Students");
            System.out.println("4. Update Student");
            System.out.println("5. Delete Student");
            System.out.println("6. Exit");

            int choice = Utils.readInt(scanner, "Enter your choice: ");

            switch (choice) {
                case 1 -> {
                    Student student = new Student();
                    student.setName(Utils.readString(scanner, "Enter student name: "));
                    student.setAge(Utils.readInt(scanner, "Enter student age: "));
                    student.setCourse(Utils.readString(scanner, "Enter course: "));
                    studentService.addStudent(student);
                    System.out.println("Student added successfully.");
                }
                case 2 -> {
                    List<Student> students = studentService.getAllStudents();
                    if (students.isEmpty()) {
                        System.out.println("No students found.");
                    } else {
                        for (Student student : students) {
                            System.out.println(student);
                        }
                    }
                }
                case 3 -> {
                    String keyword = Utils.readString(scanner, "Enter name or course to search: ");
                    List<Student> results = studentService.searchStudents(keyword);
                    if (results.isEmpty()) {
                        System.out.println("No matching students found.");
                    } else {
                        for (Student student : results) {
                            System.out.println(student);
                        }
                    }
                }
                case 4 -> {
                    int id = Utils.readInt(scanner, "Enter student ID to update: ");
                    Student existing = studentService.findById(id);
                    if (existing == null) {
                        System.out.println("Student not found.");
                    } else {
                        Student updated = new Student();
                        updated.setName(Utils.readString(scanner, "Enter new name: "));
                        updated.setAge(Utils.readInt(scanner, "Enter new age: "));
                        updated.setCourse(Utils.readString(scanner, "Enter new course: "));
                        boolean success = studentService.updateStudent(id, updated);
                        System.out.println(success ? "Student updated successfully." : "Failed to update student.");
                    }
                }
                case 5 -> {
                    int id = Utils.readInt(scanner, "Enter student ID to delete: ");
                    boolean success = studentService.deleteStudent(id);
                    System.out.println(success ? "Student deleted successfully." : "Student not found.");
                }
                case 6 -> {
                    running = false;
                    System.out.println("Exiting Student Management System.");
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }

        scanner.close();
    }
}
