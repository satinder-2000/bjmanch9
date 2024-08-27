function togglePW() {
    var field1 = document.getElementById("password");
    if (field1.type === "password") {
        field1.type = "text";
    } else {
        field1.type = "password";
    }
}
