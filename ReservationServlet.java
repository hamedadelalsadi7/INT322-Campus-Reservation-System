
package com.ajmanuniversity.campusreservation;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.math.BigDecimal;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "ReservationServlet", urlPatterns = {"/ReservationServlet"})
public class ReservationServlet extends HttpServlet {

    private static final String DB_URL =
            "jdbc:mysql://localhost:3306/campus_facility_db";

    private static final String DB_USER = "root";

    private static final String DB_PASSWORD =
            "3w4wQCwD";

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        try (PrintWriter out = response.getWriter()) {

            String userName = request.getParameter("userName");
            String userRole = request.getParameter("userRole");
            String itemCategory = request.getParameter("itemCategory");

            if (userName == null || userRole == null ||
                    itemCategory == null ||
                    request.getParameter("quantity") == null ||
                    request.getParameter("bookingDays") == null) {
                response.sendError(400, "Missing reservation details");
                return;
            }

            userName = userName.trim().toUpperCase();
            userRole = userRole.trim().toUpperCase();
            itemCategory = itemCategory.trim().toUpperCase();

            if (userName.isEmpty() ||
                    !(userRole.equals("STUDENT") ||
                      userRole.equals("FACULTY"))) {
                response.sendError(400, "Invalid user details");
                return;
            }

            int quantity;
            int bookingDays;

            try {
                quantity = Integer.parseInt(
                        request.getParameter("quantity").trim());

                bookingDays = Integer.parseInt(
                        request.getParameter("bookingDays").trim());

            } catch (NumberFormatException e) {
                response.sendError(400,
                        "Quantity and booking days must be numbers");
                return;
            }

            if (quantity < 1 || bookingDays < 1) {
                response.sendError(400,
                        "Quantity and booking days must be greater than zero");
                return;
            }

            double dailyRate;

            switch (itemCategory) {
                case "LAPTOP":
                    dailyRate = 50;
                    break;

                case "PROJECTOR":
                    dailyRate = 80;
                    break;

                case "POD":
                    dailyRate = 100;
                    break;

                default:
                    response.sendError(400, "Invalid item category");
                    return;
            }

            BigDecimal rate = BigDecimal.valueOf(dailyRate);

            BigDecimal subtotal = rate
                    .multiply(BigDecimal.valueOf(quantity))
                    .multiply(BigDecimal.valueOf(bookingDays));

            BigDecimal discount = BigDecimal.ZERO;

            if (userRole.equals("FACULTY")) {
                discount = subtotal.multiply(
                        new BigDecimal("0.15"));
            }

            BigDecimal netTotal = subtotal.subtract(discount);

            String sql = "INSERT INTO reservations "
                    + "(user_name, user_role, item_category, quantity, "
                    + "booking_days, daily_rate, subtotal, net_total) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            try {
                Class.forName("com.mysql.cj.jdbc.Driver");

                try (Connection connection = DriverManager.getConnection(
                        DB_URL, DB_USER, DB_PASSWORD);

                     PreparedStatement statement =
                             connection.prepareStatement(sql)) {

                    statement.setString(1, userName);
                    statement.setString(2, userRole);
                    statement.setString(3, itemCategory);
                    statement.setInt(4, quantity);
                    statement.setInt(5, bookingDays);
                    statement.setBigDecimal(6, rate);
                    statement.setBigDecimal(7, subtotal);
                    statement.setBigDecimal(8, netTotal);

                    statement.executeUpdate();
                }

            } catch (ClassNotFoundException | SQLException e) {
                log("Database error", e);
                response.sendError(500,
                        "Could not save reservation. Check database settings.");
                return;
            }

            out.println("<!DOCTYPE html>");
            out.println("<html><head><title>Reservation Receipt</title>");

            out.println("<style>");
            out.println("body{font-family:Arial;background:#f2f5f9;padding:30px;}");
            out.println(".box{background:white;max-width:550px;margin:auto;padding:25px;border-radius:10px;}");
            out.println("h1{color:#17365d;text-align:center;}");
            out.println("</style></head><body>");

            out.println("<div class='box'>");
            out.println("<h1>Reservation Receipt</h1>");

            out.println("<p><b>Full Name:</b> " + userName + "</p>");
            out.println("<p><b>User Role:</b> " + userRole + "</p>");
            out.println("<p><b>Item:</b> " + itemCategory + "</p>");
            out.println("<p><b>Quantity:</b> " + quantity + "</p>");
            out.println("<p><b>Booking Days:</b> " + bookingDays + "</p>");

            out.println("<hr>");

            out.println("<p><b>Daily Rate:</b> " + rate + " AED</p>");
            out.println("<p><b>Subtotal:</b> " + subtotal + " AED</p>");
            out.println("<p><b>Discount:</b> " + discount + " AED</p>");

            out.println("<h2>Net Total: " + netTotal + " AED</h2>");

            out.println("<p>Reservation saved successfully!</p>");
            out.println("<a href='index.html'>Back to Booking</a>");

            out.println("</div></body></html>");
        }
    }
}
