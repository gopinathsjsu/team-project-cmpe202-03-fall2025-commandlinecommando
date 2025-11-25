all: clean

clean:
	mvn clean

compile:
	mvn compile

run: compile
	mvn spring-boot:run

run-prod: compile
	SPRING_PROFILES_ACTIVE=prod mvn spring-boot:run

build:
	mvn package

build-prod:
	mvn clean package -DskipTests

