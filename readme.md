# Election Ring RMI Projects

## Authors
1. Pranav Chand
2. Pui Kit Chen
3. Aryan Sharma

This repository contains two different implementations of a distributed election algorithm using the Ring topology and Java RMI. Each implementation resides in its own directory and has a dedicated `README.md` file to explain the details, setup, unit tests and usage instructions for that specific version.

## Project Structure

1. **Election Ring Without Peer Register**  
   A simple implementation where each node is manually linked to its next node in the ring.

   👉 [Go to README for Election Ring Without Peer Register](https://github.com/Pranav-XP/election-ring-rmi/blob/6c36ea2acb9671fe58723d597aa36b78ddfd9400/without-peer-register/readme.md)<br></br>

2. **Election Ring With Peer Register**  
   An advanced implementation where nodes register themselves with a `PeerRegister`, which manages the ring structure and node linkage dynamically.

   👉 [Go to README for Election Ring With Peer Register](https://github.com/Pranav-XP/election-ring-rmi/blob/4867dbfc077160be3055e3ffef737297f40fa4ea/with-peer-register/readme.md)<br></br>

---

For more details, navigate to the respective project folder and review the provided `README.md` file.
