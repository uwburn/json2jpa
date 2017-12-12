package it.mgt.json2jpa;

class GlobMatcher {

    static boolean match(String expression, String path) {
        if (expression.equals("**")) {
            return true;
        }
        else if (expression.endsWith("**")) {
            expression = expression.substring(0, expression.length() - 3);

            if (path.startsWith(expression))
                return true;
        }
        else if (expression.equals("*")) {
            if (path.split("/").length <= 1)
                return true;
        }
        else if (expression.endsWith("*")) {
            expression = expression.substring(0, expression.length() - 2);
            if (path.startsWith(expression))
                if (path.substring(expression.length()).split("/").length <= 2)
                    return true;
        }
        else {
            if (expression.equals(path))
                return true;
        }

        return false;
    }

}
