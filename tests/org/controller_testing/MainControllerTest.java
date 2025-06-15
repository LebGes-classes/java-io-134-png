package org.controller_testing;

import org.example.controllers.MainController;
import org.example.university.Student;
import org.example.university.Subject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MainControllerTest {

    public static MainController controller = new MainController();
    static int groupForOp = 901;
    int newGroup = 902;


    @BeforeEach
    void initGroup(){
        controller.makeDefaultGroup(groupForOp);
    }

    @AfterAll
    static void delGroup(){
        controller.removeGroup(groupForOp);
    }
    @Test
    void groupOperation(){

        assertFalse(controller.isGroupExists(newGroup));

        controller.makeDefaultGroup(newGroup);
        assertTrue(controller.isGroupExists(newGroup));

        assertTrue(controller.removeGroup(newGroup));
        assertFalse(controller.isGroupExists(newGroup));
    }

    @Test
    void studAddingAndDeducting(){

        controller.addNewStudent(groupForOp, "Вася Петров");
        controller.addNewStudent(groupForOp, "Петр Васильев");
        List<Student> studs = controller.getStudentsFrom(groupForOp);

        studs = studs.stream().sorted().toList();

        assertEquals("Вася Петров", studs.get(0).getFullName());

        Student stud = studs.get(0);

        controller.deductStudent(groupForOp, stud.getId());


        assertFalse(controller.isStudentInGroup(stud.getId(), groupForOp));

        controller.deductStudent(groupForOp, studs.get(1).getId());
        assertTrue(controller.getStudentsFrom(groupForOp).isEmpty());

    }

    @Test
    void studPointsTesting(){
        controller.addNewStudent(groupForOp, "dimebag plugg");

        Student stud = controller.getStudentsFrom(groupForOp).get(0);
        Collection<Subject> subjs = controller.getSubjects();

        for(Subject subj: subjs){
            controller.setStudentPoints(groupForOp, stud.getId(), subj.getId(), subj.getId() * 10.0);

            assertEquals(subj.getId() * 10.0, controller.getStudentPointsFrom(groupForOp, stud.getId(), subj.getId()));
        }

        controller.deductStudent(groupForOp, stud.getId());

    }

}
