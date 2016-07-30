

function test() {
    console.log(typeof arguments[0]);
}

test(1,2,3,4);

test({"integer":1, "float":2.4});
