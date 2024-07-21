/* 
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/JavaScript.js to edit this template
 */

function togglePW() {
    var field1 = document.getElementById("password");
    if (field1.type === "password") {
        field1.type = "text";
    } else {
        field1.type = "password";
    }
}
