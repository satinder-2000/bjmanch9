function togglePWandCP() {
    var field1 = document.getElementById("password");
    if (field1.type === "password") {
        field1.type = "text";
    } else {
        field1.type = "password";
    }
    var field2= document.getElementById("confirmPassword");
    if (field2.type === "password") {
        field2.type = "text";
    } else {
        field2.type = "password";
    }
}


