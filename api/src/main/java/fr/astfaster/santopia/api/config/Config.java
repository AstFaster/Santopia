package fr.astfaster.santopia.api.config;

public interface Config {

    MongoDB mongoDB();

    Redis redis();

    RabbitMQ rabbitMQ();

    String discordURL();

    class MongoDB {

        protected String username;
        protected String password;
        protected String hostname;
        protected int port;

        public MongoDB() {
            this("", "", "", 27017);
        }

        public MongoDB(String username, String password, String hostname, int port) {
            this.username = username;
            this.password = password;
            this.hostname = hostname;
            this.port = port;
        }

        public String username() {
            return this.username;
        }

        public String password() {
            return this.password;
        }

        public String hostname() {
            return this.hostname;
        }

        public int port() {
            return this.port;
        }

        public String toURL() {
            String url = "mongodb://";

            if (this.username != null && !this.username.equals("")) {
                url += this.username;
            }

            if (this.password != null && !this.password.equals("")) {
                url += ":" + this.password;
            }

            if ((this.username != null && !this.username.equals("")) || (this.password != null && !this.password.equals(""))) {
                url += "@";
            }

            return url + (this.hostname + ":" + this.port);
        }

    }

    class Redis {

        protected String password;
        protected String hostname;
        protected int port;

        public Redis() {
            this("admin", "localhost", 6379);
        }

        public Redis(String password, String hostname, int port) {
            this.password = password;
            this.hostname = hostname;
            this.port = port;
        }


        public String password() {
            return this.password;
        }

        public String hostname() {
            return this.hostname;
        }

        public int port() {
            return this.port;
        }

    }

    class RabbitMQ {

        protected String username;
        protected String password;
        protected String hostname;
        protected int port;

        public RabbitMQ() {
            this("admin", "admin", "localhost", 5672);
        }

        public RabbitMQ(String username, String password, String hostname, int port) {
            this.username = username;
            this.password = password;
            this.hostname = hostname;
            this.port = port;
        }

        public String username() {
            return this.username;
        }

        public String password() {
            return this.password;
        }

        public String hostname() {
            return this.hostname;
        }

        public int port() {
            return this.port;
        }

    }

}
