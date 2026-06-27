import java.util.ArrayList;
import java.util.List;

public class StudentService {
    private final List<Student> students = new ArrayList<>();
    private int nextId = 1;

    public void addStudent(Student student) {
        student.setId(nextId++);
        students.add(student);
    }

    public List<Student> getAllStudents() {
        return students;
    }

    public Student findById(int id) {
        for (Student student : students) {
            if (student.getId() == id) {
                return student;
            }
        }
        return null;
    }

    public boolean updateStudent(int id, Student updatedStudent) {
        for (int i = 0; i < students.size(); i++) {
            Student current = students.get(i);
            if (current.getId() == id) {
                updatedStudent.setId(id);
                students.set(i, updatedStudent);
                return true;
            }
        }
        return false;
    }

    public boolean deleteStudent(int id) {
        return students.removeIf(student -> student.getId() == id);
    }

    public List<Student> searchStudents(String keyword) {
        List<Student> matches = new ArrayList<>();
        String search = keyword.toLowerCase();
        for (Student student : students) {
            if (student.getName().toLowerCase().contains(search)
                    || student.getCourse().toLowerCase().contains(search)) {
                matches.add(student);
            }
        }
        return matches;
    }
}
