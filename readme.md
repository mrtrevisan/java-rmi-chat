## Chat com Java RMI

### Autores:
- Mauro Roberto Trevisan (mrtrevisan)
- Ramon Godoy Izidoro (<a href="https://github.com/ramonXXII/">Ramon XXII</a>)

### Dependencies: 
- openjdk 22.0.1

### How to use:

1. Compile the .java files
```
javac -d bin src/*.java 
```

2. Start the RMI Registry at port 2020
```
rmiregistry -J-Djava.class.path=bin 2020
```

3. In another terminal tab, start the Server
```
java -cp bin ServerChat
```
* The Server IP will be show in the GUI

4. Run the User side, in yet another terminal tab, passing the server IP as argument
```
java -cp bin UserChat <server IP>
```
