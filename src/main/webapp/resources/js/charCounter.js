function limitTextArea(element, limit) {
    document.getElementById('counter').innerHTML = "Chars entered: " + element.value.length;
    if (element.value.length >= limit) {
        element.value = element.value.substring(0, limit);
    }
}


