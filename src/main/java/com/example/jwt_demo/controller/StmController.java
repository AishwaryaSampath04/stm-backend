package com.example.jwt_demo.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;





@RestController
@RequestMapping("/api/stm")
@CrossOrigin("*")
public class StmController {

        @Autowired
        private JdbcTemplate jdbcTemplate;
 
        @GetMapping("/test")
        public String test() {
                return "STM Controller Working";
        }

        

        @GetMapping("/grades")
        public List<Map<String, Object>> getGrades() {
                return jdbcTemplate.queryForList(
                                "SELECT * FROM grade_master");
        }

        @GetMapping("/students")
        public List<Map<String, Object>> getStudents() {
                return jdbcTemplate.queryForList(
                                "SELECT * FROM student");
        }
@GetMapping("/exams")
public List<Map<String, Object>> getExams() {

    return jdbcTemplate.queryForList(

            "SELECT exam_id, exam_name, exam_type, display_order " +
            "FROM exam_master " +
            "WHERE status = 'Y' " +
            "ORDER BY display_order"

    );

}     


@GetMapping("/activeAcademicYear")
public Map<String, Object> getActiveAcademicYear() {

    System.out.println("Loading Current Academic Year...");

    Map<String, Object> academicYear = jdbcTemplate.queryForMap(

            "SELECT academic_year_id, academic_year_name " +
            "FROM academic_year " +
            "WHERE current_year_flag = 'Y' " +
            "AND active_flag = 'Y'"

    );

    System.out.println("Current Academic Year : " + academicYear);

    return academicYear;

}
        // ============================================================================
        // INSERT FEE PAYMENT - HIGH SCHOOL
        // ============================================================================

        @PostMapping("/payment")
        public String addPayment(
                        @RequestBody Map<String, Object> data) {

                Integer studentId = Integer.parseInt(
                                data.get("studentId").toString());

                String admissionNo = jdbcTemplate.queryForObject(
                                "SELECT admission_no " +
                                                "FROM student " +
                                                "WHERE student_id = ?",
                                String.class,
                                studentId);

                String sql = "INSERT INTO fee_payment " +
                                "(" +
                                "student_id, " +
                                "admission_no, " +
                                "receipt_no, " +
                                "amount_paid, " +
                                "payment_date, " +
                                "payment_mode" +
                                ") " +
                                "VALUES (?, ?, ?, ?, CURRENT_DATE, ?)";

                jdbcTemplate.update(
                                sql,

                                studentId,

                                admissionNo,

                                data.get("receiptNo"),

                                Double.parseDouble(
                                                data.get("amount").toString()),

                                data.get("paymentMode"));

                return "Payment Added Successfully";
        }

        // ============================================================================
        // DISPLAY BALANCE - HIGH SCHOOL
        // ============================================================================
        @GetMapping("/balance")
        public List<Map<String, Object>> getBalance() {
                return jdbcTemplate.queryForList(
                                "SELECT * FROM vw_student_balance");
        }

        // ============================================================================
        // GET STUDENTS BY GRADE - HIGH SCHOOL
        // ============================================================================
        @GetMapping("/students/grade/{gradeId}")
        public List<Map<String, Object>> getStudentsByGrade(
                        @PathVariable Integer gradeId) {

                return jdbcTemplate.queryForList(
                                "SELECT * FROM student WHERE grade_id = ? ORDER BY student_name",
                                gradeId);
        }

        // ============================================================================
        // GET PAYMENT HISTORY STUDENTS - HIGH SCHOOL
        // ============================================================================
        @GetMapping("/payments/student/{studentId}")
        public List<Map<String, Object>> getPaymentHistory(
                        @PathVariable Integer studentId) {

                return jdbcTemplate.queryForList(
                                "SELECT " +
                                                "receipt_no, " +
                                                "payment_date, " +
                                                "payment_mode, " +
                                                "amount_paid " +
                                                "FROM fee_payment " +
                                                "WHERE student_id = ? " +
                                                "ORDER BY payment_date DESC",
                                studentId);
        }

        @GetMapping("/payments/all")
        public List<Map<String, Object>> getAllPaymentHistory() {

                String sql = "SELECT * " +
                                "FROM public.vw_all_payment_history " +
                                "ORDER BY student_name, payment_date DESC";

                return jdbcTemplate.queryForList(sql);
        }

        // ============================================================================
        // GET TEACHER ATTENDANCE DATA - HIGH SCHOOL
        // ============================================================================
        @GetMapping("/teacher-attendance-data")
        public List<Map<String, Object>> getTeacherAttendanceData(
                        @RequestParam Integer month,
                        @RequestParam Integer year) {

                String sql = "SELECT " +
                                "t.teacher_id, " +
                                "t.teacher_name, " +
                                "COALESCE(a.working_days, 0) AS working_days, " +
                                "COALESCE(a.present_days, 0) AS present_days, " +
                                "COALESCE(a.absent_days, 0) AS absent_days, " +
                                "COALESCE(a.attendance_status, 'PENDING') AS attendance_status " +
                                "FROM teachers t " +
                                "LEFT JOIN teacher_attendance a " +
                                "ON t.teacher_id = a.teacher_id " +
                                "AND a.attendance_month = ? " +
                                "AND a.attendance_year = ? " +
                                "WHERE t.active_flag = 'Y' " +
                                "AND ( " +
                                "t.joining_date IS NULL " +
                                "OR t.joining_date <= " +
                                "MAKE_DATE(?, ?, 1) + INTERVAL '1 month' - INTERVAL '1 day' " +
                                ") " +
                                "AND ( " +
                                "t.leaving_date IS NULL " +
                                "OR t.leaving_date >= MAKE_DATE(?, ?, 1) " +
                                ") " +
                                "ORDER BY t.teacher_name";

                return jdbcTemplate.queryForList(
                                sql,
                                month,
                                year,
                                year,
                                month,
                                year,
                                month);
        }
        // ============================================================================
        // INSERT STUDENT - HIGH SCHOOL
        // ============================================================================

        @PostMapping("/student")
        public String addStudent(
                        @RequestBody Map<String, Object> data) {

                Integer academicYearId = jdbcTemplate.queryForObject(

                                "SELECT academic_year_id " +
                                                "FROM academic_year " +
                                                "WHERE current_year_flag = 'Y'",

                                Integer.class

                );

                String sql = "INSERT INTO student " +
                                "(" +
                                "admission_no, " +
                                "student_name, " +
                                "parent_name, " +
                                "mobile_no, " +
                                "grade_id, " +
                                "annual_fee, " +
                                "discount_amount, " +
                                "final_fee, " +
                                "joining_date, " +
                                "academic_year_id" +
                                ") " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

                jdbcTemplate.update(

                                sql,

                                data.get("admissionNo"),

                                data.get("studentName"),

                                data.get("parentName"),

                                data.get("mobileNo"),

                                Integer.parseInt(
                                                data.get("gradeId").toString()),

                                Double.parseDouble(
                                                data.get("annualFee").toString()),

                                Double.parseDouble(
                                                data.get("discountAmount").toString()),

                                Double.parseDouble(
                                                data.get("finalFee").toString()),

                                java.sql.Date.valueOf(
                                                data.get("joiningDate").toString()),

                                academicYearId

                );
                System.out.println(sql);
                return "Student Added Successfully";

        }
        // ============================================================================
        // GET FEE PAYMENT - HIGH SCHOOL
        // ============================================================================

        @GetMapping("/payment/student/{id}")
        public List<Map<String, Object>> getStudentPayments(
                        @PathVariable Integer id) {

                return jdbcTemplate.queryForList(
                                "SELECT * FROM fee_payment WHERE student_id = ?",
                                id);
        }

        @GetMapping("/subjects")
        public List<Map<String, Object>> getSubjects() {

                String sql = "SELECT subject_id, subject_name " +
                                "FROM subject_master " +
                                "WHERE active_flag = 'Y' " +
                                "ORDER BY subject_name";

                return jdbcTemplate.queryForList(sql);
        }
        // ============================================================================
        // INSERT TEACHER - HIGH SCHOOL
        // ============================================================================

        @PostMapping("/teacher")
        public String addTeacher(
                        @RequestBody Map<String, Object> data) {

                String sql = "INSERT INTO teachers " +
                                "(" +
                                "teacher_name, " +
                                "mobile_no, " +
                                "subject_id, " +
                                "joining_date, " +
                                "monthly_salary" +
                                ") " +
                                "VALUES (?, ?, ?, ?, ?)";

                jdbcTemplate.update(
                                sql,

                                data.get("teacherName"),

                                data.get("mobileNo"),

                                Integer.parseInt(
                                                data.get("subjectId").toString()),

                                java.sql.Date.valueOf(
                                                data.get("joiningDate").toString()),

                                Double.parseDouble(
                                                data.get("monthlySalary").toString()));

                return "Teacher Added Successfully";
        }
        // ============================================================================
        // INSERT TEACHERS ATTENDANCE PAYMENT - HIGH SCHOOL
        // ============================================================================

        @PostMapping("/teacher-attendance")
        public String saveAttendance(
                        @RequestBody Map<String, Object> data) {

                String sql = "INSERT INTO teacher_attendance " +
                                "(teacher_id, attendance_month, attendance_year, " +
                                "working_days, present_days, absent_days, attendance_status) " +
                                "VALUES (?, ?, ?, ?, ?, ?, 'SAVED')";

                jdbcTemplate.update(
                                sql,

                                Integer.parseInt(data.get("teacherId").toString()),

                                Integer.parseInt(data.get("month").toString()),

                                Integer.parseInt(data.get("year").toString()),

                                Integer.parseInt(data.get("workingDays").toString()),

                                Integer.parseInt(data.get("presentDays").toString()),

                                Integer.parseInt(data.get("absentDays").toString()));
                System.out.println(sql);
                return "Attendance Saved Successfully";

        }
        // ============================================================================
        // GET TEACHERS'S DATA FOR SALALRY PAYMENT - HIGH SCHOOL
        // ============================================================================

     @GetMapping("/teacher-salary-data")
public List<Map<String, Object>> getTeacherSalaryData(
        @RequestParam Integer month,
        @RequestParam Integer year) {

    String sql = "SELECT " +
            "t.teacher_id, " +
            "t.teacher_name, " +
            "t.monthly_salary, " +
            "a.working_days, " +
            "a.present_days, " +
            "a.absent_days, " +
            "COALESCE(tp.loss_of_pay_days, 0) AS loss_of_pay_days, " +
            "COALESCE(tp.extra_pay, 0) AS extra_pay, " +
            "COALESCE(tp.comments, '') AS comments, " +
            "COALESCE(tp.payable_salary, t.monthly_salary) AS payable_salary, " +
            "COALESCE(tp.payment_status, 'PENDING') AS payment_status " +
            "FROM teachers t " +
            "JOIN teacher_attendance a " +
            "ON t.teacher_id = a.teacher_id " +
            "LEFT JOIN teacher_payment tp " +
            "ON tp.teacher_id = t.teacher_id " +
            "AND tp.payment_month = ? " +
            "AND tp.payment_year = ? " +
            "WHERE a.attendance_month = ? " +
            "AND a.attendance_year = ? " +
            "ORDER BY t.teacher_name";

    return jdbcTemplate.queryForList(
            sql,
            month,
            year,
            month,
            year);
}
        // ============================================================================
        // MAKE TEACHERS'S SALALRY PAYMENT - HIGH SCHOOL
        // ============================================================================
@PostMapping("/teacher-salary")
public String processTeacherSalary(
        @RequestBody Map<String, Object> data) {

    try {

        String sql = "INSERT INTO teacher_payment " +
                "(teacher_id, payment_month, payment_year, " +
                "working_days, present_days, absent_days, loss_of_pay_days, " +
                "fixed_salary, extra_pay, comments, payable_salary, " +
                "amount_paid, payment_status) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?, 'PAID')";

        System.out.println("\n=================================================");
        System.out.println("Teacher Salary Processing");
        System.out.println("=================================================");

        System.out.println("Incoming Payload:");
        data.forEach((key, value) ->
                System.out.println(key + " = " + value));

        System.out.println("\nSQL:");
        System.out.println(sql);

        System.out.println("\nParameters:");
        System.out.println("1. teacherId      : " + data.get("teacherId"));
        System.out.println("2. paymentMonth   : " + data.get("paymentMonth"));
        System.out.println("3. paymentYear    : " + data.get("paymentYear"));
        System.out.println("4. workingDays    : " + data.get("workingDays"));
        System.out.println("5. presentDays    : " + data.get("presentDays"));
        System.out.println("6. absentDays     : " + data.get("absentDays"));
        System.out.println("7. lossOfPayDays  : " + data.get("lossOfPayDays"));
        System.out.println("8. fixedSalary    : " + data.get("fixedSalary"));
        System.out.println("9. extraPay       : " + data.get("extraPay"));
        System.out.println("10. comments      : " + data.get("comments"));
        System.out.println("11. payableSalary : " + data.get("payableSalary"));
        System.out.println("12. amountPaid    : " + data.get("payableSalary"));

        jdbcTemplate.update(
                sql,

                Integer.parseInt(data.get("teacherId").toString()),

                Integer.parseInt(data.get("paymentMonth").toString()),

                Integer.parseInt(data.get("paymentYear").toString()),

                Integer.parseInt(data.get("workingDays").toString()),

                Integer.parseInt(data.get("presentDays").toString()),

                Integer.parseInt(data.get("absentDays").toString()),

                Integer.parseInt(data.get("lossOfPayDays").toString()),

                Double.parseDouble(data.get("fixedSalary").toString()),

                Double.parseDouble(data.get("extraPay").toString()),

                data.get("comments") == null
                        ? null
                        : data.get("comments").toString(),

                Double.parseDouble(data.get("payableSalary").toString()),

                Double.parseDouble(data.get("payableSalary").toString()));

        System.out.println("\nSalary processed successfully.");
        System.out.println("=================================================\n");

        return "Salary Processed";

    } catch (Exception e) {

        System.out.println("\n================ ERROR ================");
        System.out.println("Error while processing teacher salary");
        System.out.println("Message: " + e.getMessage());
        e.printStackTrace();
        System.out.println("=======================================\n");

        throw e;
    }
}
        // ============================================================================
        // ANALYTICS DASHBOARD
        // ============================================================================

        @GetMapping("/analytics-dashboard")
        public Map<String, Object> getAnalyticsDashboard() {

                Map<String, Object> response = jdbcTemplate.queryForMap(
                                "SELECT * FROM vw_analytics_dashboard");

                double totalRevenue = ((Number) response.get("total_revenue"))
                                .doubleValue();

                double totalSalaryPaid = ((Number) response.get("total_salary_paid"))
                                .doubleValue();

                double netProfit = totalRevenue - totalSalaryPaid;

                response.put(
                                "netProfit",
                                netProfit);

                return response;
        }

        @GetMapping("/student-details")
        public List<Map<String, Object>> getStudentDetails() {

                String sql = "SELECT " +
                                "s.student_id, " +
                                "s.admission_no, " +
                                "s.student_name, " +
                                "s.parent_name, " +
                                "s.mobile_no, " +
                                "s.grade_id, " +
                                "g.grade_name, " +
                                "s.annual_fee, " +
                                "s.discount_amount, " +
                                "s.final_fee, " +
                                "s.active_flag, " +
                                "s.admission_date, " +
                                "s.joining_date, " +
                                "s.leaving_date " +
                                "FROM student s " +
                                "JOIN grade_master g " +
                                "ON s.grade_id = g.grade_id " +
                                "ORDER BY " +
                                "CASE WHEN s.active_flag = 'Y' THEN 0 ELSE 1 END, " +
                                "g.grade_name, " +
                                "s.student_name";

                System.out.println("===== Student Details SQL =====");
                System.out.println(sql);

                return jdbcTemplate.queryForList(sql);

        }

        @PutMapping("/student")
        public String updateStudent(
                        @RequestBody Map<String, Object> data) {

                Object joiningDate = (data.get("joining_date") == null ||
                                data.get("joining_date").toString().trim().isEmpty())
                                                ? null
                                                : java.sql.Date.valueOf(
                                                                data.get("joining_date").toString());

                Object leavingDate = (data.get("leaving_date") == null ||
                                data.get("leaving_date").toString().trim().isEmpty())
                                                ? null
                                                : java.sql.Date.valueOf(
                                                                data.get("leaving_date").toString());

                String sql = "UPDATE student SET " +
                                "admission_no = ?, " +
                                "student_name = ?, " +
                                "parent_name = ?, " +
                                "mobile_no = ?, " +
                                "grade_id = ?, " +
                                "annual_fee = ?, " +
                                "discount_amount = ?, " +
                                "final_fee = ?, " +
                                "active_flag = ?, " +
                                "joining_date = ?, " +
                                "leaving_date = ? " +
                                "WHERE student_id = ?";

                jdbcTemplate.update(

                                sql,

                                data.get("admission_no"),

                                data.get("student_name"),

                                data.get("parent_name"),

                                data.get("mobile_no"),

                                Integer.parseInt(
                                                data.get("grade_id").toString()),

                                Double.parseDouble(
                                                data.get("annual_fee").toString()),

                                Double.parseDouble(
                                                data.get("discount_amount").toString()),

                                Double.parseDouble(
                                                data.get("final_fee").toString()),

                                data.get("active_flag"),

                                joiningDate,

                                leavingDate,

                                Integer.parseInt(
                                                data.get("student_id").toString())

                );

                return "Student Updated Successfully";
        }
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////
        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        // ============================================================================
        // PRIMARY API CALLS
        // ============================================================================

        ///////////////////////////////////////////////////////////////////////////////////////////////////////////
        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        // ============================================================================
        // GET PRIMARY - PRIMARY
        // ============================================================================

        @GetMapping("/primary-grades")
        public List<Map<String, Object>> getPrimaryGrades() {

                String sql = "SELECT " +
                                "grade_id, " +
                                "grade_name " +
                                "FROM primary_grade_master " +
                                "WHERE active_flag = 'Y' " +
                                "ORDER BY grade_id";

                return jdbcTemplate.queryForList(sql);

        }

        // ============================================================================
        // INSERT STUDENT - PRIMARY
        // ============================================================================

       @PostMapping("/primary-student")
public String addPrimaryStudent(
        @RequestBody Map<String, Object> data) {

    Integer academicYearId = jdbcTemplate.queryForObject(

            "SELECT academic_year_id " +
            "FROM primary_academic_year " +
            "WHERE current_year_flag = 'Y'",

            Integer.class

    );

    String sql =
            "INSERT INTO primary_student " +
            "(" +
            "admission_no, " +
            "student_name, " +
            "grade_id, " +
            "parent_name, " +
            "mobile_no, " +
            "monthly_fee, " +
            "joining_date, " +
            "active_flag, " +
            "academic_year_id" +
            ") " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    System.out.println("Primary Student Request: " + data);

    jdbcTemplate.update(

            sql,

            data.get("admissionNo"),

            data.get("studentName"),

            Integer.parseInt(
                    data.get("gradeId").toString()),

            data.get("parentName"),

            data.get("mobileNo"),

            Double.parseDouble(
                    data.get("monthlyFee").toString()),

            java.sql.Date.valueOf(
                    data.get("joiningDate").toString()),

            data.getOrDefault(
                    "activeFlag",
                    "Y"),

            academicYearId

    );

    System.out.println(sql);

    return "Primary Student Added Successfully";

}
        // ================================================= ===========================
        // GET STUDENTS FOR FEE PAYMENT - PRIMARY SHOULD CHECK THE USE
        // ============================================================================

        @GetMapping("/primary-students")
        public List<Map<String, Object>> getPrimaryStudents() {

                return jdbcTemplate.queryForList(

                                "SELECT " +
                                                "student_id, " +
                                                "admission_no, " +
                                                "student_name " +
                                                "FROM primary_student " +
                                                "WHERE active_flag='Y' " +
                                                "ORDER BY student_name"

                );

        }

        // ================================================= ===========================
        // GET STUDENTS BY GRADE FOR FEE PAYMENT - PRIMARY
        // ============================================================================
        @GetMapping("/primary-students/grade/{gradeId}")
        public List<Map<String, Object>> getPrimaryStudentsByGrade(
                        @PathVariable Integer gradeId) {

                return jdbcTemplate.queryForList(

                                "SELECT " +
                                                "student_id, " +
                                                "admission_no, " +
                                                "student_name " +
                                                "FROM primary_student " +
                                                "WHERE grade_id = ? " +
                                                "AND active_flag = 'Y' " +
                                                "ORDER BY student_name",

                                gradeId

                );

        }

        // ============================================================================
        // INSERT STUDENT FEE PAYMENT - PRIMARY
        // ============================================================================
        @PostMapping("/primary-payment")
        public String addPrimaryPayment(
                        @RequestBody Map<String, Object> data) {

                String sql = "INSERT INTO primary_fee_payment " +
                                "(" +
                                "student_id, " +
                                "receipt_no, " +
                                "amount_paid, " +
                                "payment_date, " +
                                "payment_mode, " +
                                "remarks" +
                                ") " +
                                "VALUES (?, ?, ?, CURRENT_DATE, ?, ?)";

                jdbcTemplate.update(

                                sql,

                                Integer.parseInt(
                                                data.get("studentId").toString()),

                                data.get("receiptNo"),

                                Double.parseDouble(
                                                data.get("amount").toString()),

                                data.get("paymentMode"),

                                data.get("remarks")

                );

                return "Payment Added Successfully";
        }

        // ============================================================================
        // VIEW PRIMMARY BALANCE SHEET PAYMENT - PRIMARY
        // ============================================================================
        @GetMapping("/primary-balance")
        public List<Map<String, Object>> getPrimaryBalance() {

                return jdbcTemplate.queryForList(

                                "SELECT * " +
                                                "FROM vw_primary_student_balance " +
                                                "ORDER BY balance_amount DESC"

                );

        }

        // ============================================================================
        // GET PAYMENT HISTORY - PRIMARY FOR UI
        // ============================================================================
        @GetMapping("/primary-payment-history/{studentId}")
        public List<Map<String, Object>> getPrimaryPaymentHistory(
                        @PathVariable Integer studentId) {

                return jdbcTemplate.queryForList(

                                "SELECT " +
                                                "payment_date, " +
                                                "payment_mode, " +
                                                "amount_paid, " +
                                                "receipt_no," +
                                                "remarks " +
                                                "FROM primary_fee_payment " +
                                                "WHERE student_id = ? " +
                                                "ORDER BY payment_date DESC",

                                studentId

                );

        }

        // ============================================================================
        // GET ALL PAYMENT HISTORY - PRIMARY FOR DOWNLOAD AS EXCEL
        // ============================================================================
        @GetMapping("/primary-payment-history/all")
        public List<Map<String, Object>> getAllPrimaryPaymentHistory() {

                return jdbcTemplate.queryForList(

                                "SELECT * " +
                                                "FROM vw_primary_payment_history"

                );

        }

        @GetMapping("/primary-subjects")
        public List<Map<String, Object>> getPrimarySubjects() {

                return jdbcTemplate.queryForList(

                                "SELECT " +
                                                "subject_id, " +
                                                "subject_name " +
                                                "FROM primary_subject_master " +
                                                "WHERE active_flag = 'Y' " +
                                                "ORDER BY subject_name"

                );

        }

        @PostMapping("/primary-teacher")
        public String addPrimaryTeacher(
                        @RequestBody Map<String, Object> data) {

                String sql = "INSERT INTO primary_teachers (" +
                                "teacher_name, " +
                                "mobile_no, " +
                                "subject_id, " +
                                "monthly_salary, " +
                                "joining_date, " +
                                "active_flag" +
                                ") VALUES (?, ?, ?, ?, ?, ?)";

                jdbcTemplate.update(

                                sql,

                                data.get("teacherName"),

                                data.get("mobileNo"),

                                Integer.parseInt(
                                                data.get("subjectId").toString()),

                                Double.parseDouble(
                                                data.get("monthlySalary").toString()),

                                java.sql.Date.valueOf(
                                                data.get("joiningDate").toString()),

                                data.get("activeFlag")

                );

                return "Primary Teacher Added Successfully";

        }

        @GetMapping("/primary-teacher-attendance-data")
        public List<Map<String, Object>> getPrimaryTeacherAttendanceData(
                        @RequestParam Integer month,
                        @RequestParam Integer year) {

                String sql = "SELECT " +
                                "t.teacher_id, " +
                                "t.teacher_name, " +
                                "COALESCE(a.working_days, 0) AS working_days, " +
                                "COALESCE(a.present_days, 0) AS present_days, " +
                                "COALESCE(a.absent_days, 0) AS absent_days, " +
                                "COALESCE(a.attendance_status, 'PENDING') AS attendance_status " +
                                "FROM primary_teachers t " +
                                "LEFT JOIN primary_teacher_attendance a " +
                                "ON t.teacher_id = a.teacher_id " +
                                "AND a.attendance_month = ? " +
                                "AND a.attendance_year = ? " +
                                "WHERE t.active_flag = 'Y' " +
                                "AND ( " +
                                "t.joining_date IS NULL " +
                                "OR t.joining_date <= " +
                                "MAKE_DATE(?, ?, 1) + INTERVAL '1 month' - INTERVAL '1 day' " +
                                ") " +
                                "AND ( " +
                                "t.leaving_date IS NULL " +
                                "OR t.leaving_date >= MAKE_DATE(?, ?, 1) " +
                                ") " +
                                "ORDER BY t.teacher_name";

                return jdbcTemplate.queryForList(
                                sql,
                                month,
                                year,
                                year,
                                month,
                                year,
                                month);
        }

        @PostMapping("/primary-teacher-attendance")
        public String savePrimaryAttendance(
                        @RequestBody Map<String, Object> data) {

                String sql = "INSERT INTO primary_teacher_attendance " +
                                "(" +
                                "teacher_id, " +
                                "attendance_month, " +
                                "attendance_year, " +
                                "working_days, " +
                                "present_days, " +
                                "absent_days, " +
                                "attendance_status" +
                                ") " +
                                "VALUES (?, ?, ?, ?, ?, ?, 'SAVED')";

                jdbcTemplate.update(

                                sql,

                                Integer.parseInt(
                                                data.get("teacherId")
                                                                .toString()),

                                Integer.parseInt(
                                                data.get("month")
                                                                .toString()),

                                Integer.parseInt(
                                                data.get("year")
                                                                .toString()),

                                Integer.parseInt(
                                                data.get("workingDays")
                                                                .toString()),

                                Integer.parseInt(
                                                data.get("presentDays")
                                                                .toString()),

                                Integer.parseInt(
                                                data.get("absentDays")
                                                                .toString())

                );

                System.out.println(sql);

                return "Attendance Saved Successfully";

        }

      @GetMapping("/primary-teacher-salary-data")
public List<Map<String, Object>> getPrimaryTeacherSalaryData(
        @RequestParam Integer month,
        @RequestParam Integer year) {

    String sql = "SELECT " +
            "t.teacher_id, " +
            "t.teacher_name, " +
            "t.monthly_salary, " +
            "a.working_days, " +
            "a.present_days, " +
            "a.absent_days, " +
            "COALESCE(tp.loss_of_pay_days, 0) AS loss_of_pay_days, " +
            "COALESCE(tp.extra_pay, 0) AS extra_pay, " +
            "COALESCE(tp.comments, '') AS comments, " +
            "COALESCE(tp.payable_salary, t.monthly_salary) AS payable_salary, " +
            "COALESCE(tp.payment_status, 'PENDING') AS payment_status " +
            "FROM primary_teachers t " +
            "JOIN primary_teacher_attendance a " +
            "ON t.teacher_id = a.teacher_id " +
            "LEFT JOIN primary_teacher_payment tp " +
            "ON tp.teacher_id = t.teacher_id " +
            "AND tp.payment_month = ? " +
            "AND tp.payment_year = ? " +
            "WHERE a.attendance_month = ? " +
            "AND a.attendance_year = ? " +
            "AND t.active_flag = 'Y' " +
            "ORDER BY t.teacher_name";

    System.out.println("===========================================");
    System.out.println("Primary Teacher Salary Data");
    System.out.println("===========================================");
    System.out.println("Month : " + month);
    System.out.println("Year  : " + year);
    System.out.println("SQL   : " + sql);

    List<Map<String, Object>> result = jdbcTemplate.queryForList(
            sql,
            month,
            year,
            month,
            year);

    System.out.println("Records Found : " + result.size());

    result.forEach(System.out::println);

    return result;
}

      @PostMapping("/primary-teacher-salary")
public String processPrimaryTeacherSalary(
        @RequestBody Map<String, Object> data) {

    try {

        String sql = "INSERT INTO primary_teacher_payment " +
                "(" +
                "teacher_id, " +
                "payment_month, " +
                "payment_year, " +
                "working_days, " +
                "loss_of_pay_days, " +
                "fixed_salary, " +
                "extra_pay, " +
                "comments, " +
                "payable_salary, " +
                "amount_paid, " +
                "payment_status, " +
                "present_days, " +
                "absent_days" +
                ") " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'PAID', ?, ?)";

        System.out.println("\n=========================================");
        System.out.println("Primary Teacher Salary Processing");
        System.out.println("=========================================");

        System.out.println("Incoming Payload:");
        data.forEach((key, value) ->
                System.out.println(key + " = " + value));

        System.out.println("\nSQL:");
        System.out.println(sql);

        jdbcTemplate.update(

                sql,

                Integer.parseInt(data.get("teacherId").toString()),

                Integer.parseInt(data.get("paymentMonth").toString()),

                Integer.parseInt(data.get("paymentYear").toString()),

                Integer.parseInt(data.get("workingDays").toString()),

                Integer.parseInt(data.get("lossOfPayDays").toString()),

                Double.parseDouble(data.get("fixedSalary").toString()),

                Double.parseDouble(data.get("extraPay").toString()),

                data.get("comments") == null
                        ? null
                        : data.get("comments").toString(),

                Double.parseDouble(data.get("payableSalary").toString()),

                Double.parseDouble(data.get("payableSalary").toString()),

                Integer.parseInt(data.get("presentDays").toString()),

                Integer.parseInt(data.get("absentDays").toString())

        );

        System.out.println("Primary Teacher Salary processed successfully.");
        System.out.println("=========================================\n");

        return "Salary Processed Successfully";

    } catch (Exception e) {

        System.out.println("\n============== ERROR ==============");
        System.out.println("Error while processing Primary Teacher Salary");
        System.out.println("Message : " + e.getMessage());
        e.printStackTrace();
        System.out.println("===================================\n");

        throw e;
    }

}
        @GetMapping("/primary-analytics-dashboard")
        public Map<String, Object> getPrimaryAnalyticsDashboard() {

                String sql = "SELECT * " +
                                "FROM vw_primary_analytics_dashboard";

                System.out.println(sql);

                return jdbcTemplate.queryForMap(sql);

        }

        @GetMapping("/primary-student-details")
        public List<Map<String, Object>> getPrimaryStudentDetails() {

                String sql = "SELECT " +
                                "s.student_id, " +
                                "s.admission_no, " +
                                "s.student_name, " +
                                "s.parent_name, " +
                                "s.mobile_no, " +
                                "s.grade_id, " +
                                "g.grade_name, " +
                                "s.monthly_fee, " +
                                "s.active_flag, " +
                                "s.created_date, " +
                                "s.joining_date, " +
                                "s.leaving_date " +
                                "FROM primary_student s " +
                                "JOIN primary_grade_master g " +
                                "ON s.grade_id = g.grade_id " +
                                "ORDER BY " +
                                "CASE WHEN s.active_flag = 'Y' THEN 0 ELSE 1 END, " +
                                "g.grade_name, " +
                                "s.student_name";

                System.out.println("===== Primary Student Details SQL =====");
                System.out.println(sql);

                return jdbcTemplate.queryForList(sql);

        }

        @PutMapping("/primary-student")
        public String updatePrimaryStudent(
                        @RequestBody Map<String, Object> data) {

                Object joiningDate = (data.get("joining_date") == null ||
                                data.get("joining_date").toString().trim().isEmpty())
                                                ? null
                                                : java.sql.Date.valueOf(
                                                                data.get("joining_date").toString());

                Object leavingDate = (data.get("leaving_date") == null ||
                                data.get("leaving_date").toString().trim().isEmpty())
                                                ? null
                                                : java.sql.Date.valueOf(
                                                                data.get("leaving_date").toString());

                String sql = "UPDATE primary_student SET " +
                                "admission_no = ?, " +
                                "student_name = ?, " +
                                "parent_name = ?, " +
                                "mobile_no = ?, " +
                                "grade_id = ?, " +
                                "monthly_fee = ?, " +
                                "active_flag = ?, " +
                                "joining_date = ?, " +
                                "leaving_date = ? " +
                                "WHERE student_id = ?";

                jdbcTemplate.update(

                                sql,

                                data.get("admission_no"),

                                data.get("student_name"),

                                data.get("parent_name"),

                                data.get("mobile_no"),

                                Integer.parseInt(
                                                data.get("grade_id").toString()),

                                Double.parseDouble(
                                                data.get("monthly_fee").toString()),

                                data.get("active_flag"),

                                joiningDate,

                                leavingDate,

                                Integer.parseInt(
                                                data.get("student_id").toString())

                );

                return "Primary Student Updated Successfully";

        }




@SuppressWarnings("unchecked")
@PostMapping("/saveMarks")
public String saveMarks(
        @RequestBody Map<String, Object> request) {

    try {

        System.out.println("=======================================");
        System.out.println("SAVE MARKS STARTED");
        System.out.println("=======================================");

        Integer examId =
                Integer.parseInt(request.get("examId").toString());

        Integer gradeId =
                Integer.parseInt(request.get("gradeId").toString());

        Integer academicYearId =
                Integer.parseInt(request.get("academicYearId").toString());

        List<Map<String, Object>> subjects =
                (List<Map<String, Object>>) request.get("subjects");

        List<Map<String, Object>> students =
                (List<Map<String, Object>>) request.get("students");

        System.out.println("Exam Id : " + examId);
        System.out.println("Grade Id : " + gradeId);
        System.out.println("Academic Year Id : " + academicYearId);

        //------------------------------------------------------
        // CHECK EXAM ENTRY
        //------------------------------------------------------

        List<Map<String, Object>> entry =
                jdbcTemplate.queryForList(

                        "SELECT exam_entry_id " +
                                "FROM exam_entry " +
                                "WHERE exam_id=? " +
                                "AND grade_id=? " +
                                "AND academic_year_id=?",

                        examId,
                        gradeId,
                        academicYearId);

        Integer examEntryId;

        if (entry.isEmpty()) {

                System.out.println("Creating New Exam Entry");

                jdbcTemplate.update(

                                "INSERT INTO exam_entry " +
                                                "(exam_id,grade_id,academic_year_id,status) " +
                                                "VALUES (?,?,?,?)",

                                examId,
                                gradeId,
                                academicYearId,
                                "DRAFT");

                examEntryId = jdbcTemplate.queryForObject(

                                "SELECT exam_entry_id " +
                                                "FROM exam_entry " +
                                                "WHERE exam_id=? " +
                                                "AND grade_id=? " +
                                                "AND academic_year_id=?",

                                Integer.class,

                                examId,
                                gradeId,
                                academicYearId);

        } else {

                examEntryId = Integer.parseInt(

                                entry.get(0)
                                                .get("exam_entry_id")
                                                .toString());

                System.out.println("Existing Exam Entry : " + examEntryId);

                jdbcTemplate.update(

                                "DELETE FROM exam_marks " +
                                                "WHERE exam_entry_id=?",

                                examEntryId);

        }

        //------------------------------------------------------
        // SAVE MARKS
        //------------------------------------------------------

        for (Map<String, Object> student : students) {

                Integer studentId =
                                Integer.parseInt(
                                                student.get("student_id").toString());

                Map<String, Object> marks =
                                (Map<String, Object>) student.get("marks");

                for (Map<String, Object> subject : subjects) {

                        Integer subjectId =
                                        Integer.parseInt(
                                                        subject.get("subject_id").toString());

                        Double maximumMarks =
                                        Double.parseDouble(
                                                        subject.get("maximumMarks").toString());

                        Double passingMarks =
                                        Double.parseDouble(
                                                        subject.get("passingMarks").toString());

                        Object enteredMark =
                                        marks.get(String.valueOf(subjectId));

                        Double marksObtained = null;

                        if (enteredMark != null &&
                                        !enteredMark.toString().trim().isEmpty()) {

                                marksObtained =
                                                Double.parseDouble(
                                                                enteredMark.toString());

                        }

                        System.out.println("--------------------------------");

                        System.out.println("Student Id : " + studentId);

                        System.out.println("Subject Id : " + subjectId);

                        System.out.println("Maximum Marks : " + maximumMarks);

                        System.out.println("Passing Marks : " + passingMarks);

                        System.out.println("Marks Obtained : " + marksObtained);

                        jdbcTemplate.update(

                                        "INSERT INTO exam_marks " +

                                                        "(exam_entry_id," +
                                                        "student_id," +
                                                        "subject_id," +
                                                        "maximum_marks," +
                                                        "passing_marks," +
                                                        "marks_obtained) " +

                                                        "VALUES (?,?,?,?,?,?)",

                                        examEntryId,
                                        studentId,
                                        subjectId,
                                        maximumMarks,
                                        passingMarks,
                                        marksObtained);

                }

        }

        System.out.println("=======================================");
        System.out.println("MARKS SAVED SUCCESSFULLY");
        System.out.println("=======================================");

        return "Marks Saved Successfully";

    }

    catch (Exception e) {

        System.out.println("=======================================");
        System.out.println("SAVE FAILED");
        System.out.println("=======================================");

        e.printStackTrace();

        return e.getMessage();

    }

}



@PutMapping("/finalSubmit")
public String finalSubmit(
        @RequestBody Map<String,Object> data) {

    Integer examId =
            Integer.parseInt(data.get("examId").toString());

    Integer gradeId =
            Integer.parseInt(data.get("gradeId").toString());

    Integer academicYearId =
            Integer.parseInt(data.get("academicYearId").toString());

    Integer examEntryId = jdbcTemplate.queryForObject(

            "SELECT exam_entry_id " +
            "FROM exam_entry " +
            "WHERE exam_id=? " +
            "AND grade_id=? " +
            "AND academic_year_id=?",

            Integer.class,

            examId,
            gradeId,
            academicYearId);

    Integer count = jdbcTemplate.queryForObject(

            "SELECT COUNT(*) FROM exam_marks WHERE exam_entry_id=?",

            Integer.class,

            examEntryId);

    if(count==0){

        return "No marks available.";

    }

    jdbcTemplate.update(

            "UPDATE exam_entry " +
            "SET status='FINAL',submitted_date=NOW() " +
            "WHERE exam_entry_id=?",

            examEntryId);

    return "Final Submitted Successfully";

}
 


@GetMapping("/examMarks")
public Map<String, Object> getExamMarks(

        @RequestParam Integer examId,

        @RequestParam Integer gradeId,

        @RequestParam Integer academicYearId) {

    Map<String, Object> response = new HashMap<>();

    //----------------------------------------------------------
    // CHECK WHETHER EXAM ENTRY EXISTS
    //----------------------------------------------------------

    List<Map<String, Object>> entry = jdbcTemplate.queryForList(

            "SELECT exam_entry_id, status " +
                    "FROM exam_entry " +
                    "WHERE exam_id=? " +
                    "AND grade_id=? " +
                    "AND academic_year_id=?",

            examId,
            gradeId,
            academicYearId);

    if (entry.isEmpty()) {

        response.put("exists", false);

        return response;

    }

    //----------------------------------------------------------
    // GET EXAM ENTRY DETAILS
    //----------------------------------------------------------

    Integer examEntryId = Integer.parseInt(

            entry.get(0)
                    .get("exam_entry_id")
                    .toString());

    String status =
            entry.get(0)
                    .get("status")
                    .toString();

    response.put("exists", true);

    response.put("examEntryId", examEntryId);

    response.put("status", status);

    //----------------------------------------------------------
    // LOAD SAVED MARKS
    //----------------------------------------------------------

    List<Map<String, Object>> marks = jdbcTemplate.queryForList(

            "SELECT " +
                    "student_id, " +
                    "subject_id, " +
                    "maximum_marks, " +
                    "passing_marks, " +
                    "marks_obtained " +
                    "FROM exam_marks " +
                    "WHERE exam_entry_id=? " +
                    "ORDER BY student_id, subject_id",

            examEntryId);

    response.put("marks", marks);

    System.out.println("==================================");
    System.out.println("LOAD EXAM MARKS");
    System.out.println("==================================");
    System.out.println("Exam Entry Id : " + examEntryId);
    System.out.println("Status        : " + status);
    System.out.println("Marks Count   : " + marks.size());
    System.out.println("==================================");

    return response;

}



@GetMapping("/classMarks")
public Map<String, Object> getClassMarks(

        @RequestParam Integer examId,

        @RequestParam Integer gradeId,

        @RequestParam Integer academicYearId) {

    Map<String, Object> response = new HashMap<>();

    //-------------------------------------------------------
    // Exam
    //-------------------------------------------------------

    Map<String, Object> exam = jdbcTemplate.queryForMap(

            "SELECT exam_name " +
                    "FROM exam_master " +
                    "WHERE exam_id=?",

            examId);

    //-------------------------------------------------------
    // Grade
    //-------------------------------------------------------

    Map<String, Object> grade = jdbcTemplate.queryForMap(

            "SELECT grade_name " +
                    "FROM grade_master " +
                    "WHERE grade_id=?",

            gradeId);

    //-------------------------------------------------------
    // Academic Year
    //-------------------------------------------------------

    Map<String, Object> year = jdbcTemplate.queryForMap(

            "SELECT academic_year_name " +
                    "FROM academic_year " +
                    "WHERE academic_year_id=?",

            academicYearId);

    //-------------------------------------------------------
    // Subjects
    //-------------------------------------------------------

    List<Map<String, Object>> subjects = jdbcTemplate.queryForList(

            "SELECT subject_id, subject_name " +
                    "FROM subject_master " +
                    "ORDER BY subject_name");

    //-------------------------------------------------------
    // Students + Marks
    //-------------------------------------------------------

  List<Map<String, Object>> marks = jdbcTemplate.queryForList(

        "SELECT " +

                "s.admission_no, " +
                "s.student_name, " +

                "sm.subject_id, " +
                "sub.subject_name, " +

                "em.maximum_marks, " +
                "em.passing_marks, " +
                "em.marks_obtained " +

                "FROM exam_marks em " +

                "INNER JOIN student s " +
                "ON em.student_id=s.student_id " +

                "INNER JOIN subject_master sub " +
                "ON em.subject_id=sub.subject_id " +

                "INNER JOIN exam_entry ee " +
                "ON em.exam_entry_id=ee.exam_entry_id " +

                "INNER JOIN subject_master sm " +
                "ON em.subject_id=sm.subject_id " +

                "WHERE ee.exam_id=? " +
                "AND ee.grade_id=? " +
                "AND ee.academic_year_id=? " +

                "ORDER BY s.admission_no, sm.subject_id",

        examId,
        gradeId,
        academicYearId);
    //-------------------------------------------------------
    // Response
    //-------------------------------------------------------

    response.put("exam",

            exam.get("exam_name"));

    response.put("grade",

            grade.get("grade_name"));

    response.put("academicYear",

            year.get("academic_year_name"));

    response.put("subjects",

            subjects);

    response.put("marks",

            marks);

    return response;

}


}