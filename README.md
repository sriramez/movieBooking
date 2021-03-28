# movieBooking


1.To use this application first start mongodb in localhost:27017 without any username and password.

2.Then run mvn clean install inside the MovieTheaterBookingWithScheduler Folder.(MovieTheaterBookingWithCache is incomplete)

3.Run Java -jar movieTheater-1.0.0-SNAPSHOT.jar to run the application.

4. One the application started go to http://localhost:8080/swagger-ui.html.

5. To use the application first go to theater controller and create a theater with theater name,movienames,timings][assume 0 = 9AM-12AM,1=12.30PM -2PM],
  rows and column represent the number of seats in the theater if rows=10, column=10 then geerated seats will be A1...A20, to J1...J20.

6. Then use the create user api to create a user.

7. Then use the login api to login using credentials once logged in the session will be maintained for 10 mins.(used cache time to live);

8. User /movie api to render the seat states similar to bookmyshow.

9. To book tickets use /blocktickets api to pass movie name,tickets,timings,userid,username to block the tickets so that other people cannot book it for 2 mins and if payment is not made in 2 mins it will be allowed to other people to book it. use uuid returned in the api to be passed to the payment api.
NOTE: in the seat object there are 2 states a seat can be in they are isBooked and isConfirmed is ammount is payed then both will be 1 or only isbooked will be 1 and after 2 mins is payment is not made it will be back to 0.

10. In the payment api pass the necessary details along with the uuid from previous api to finish the transaction.

11.Once its successfull use the /movie api to verify if its mapped to the correct user and is booked and is confirmed values are 1.

12. To run multiple instances of same application we can use docker-swarm or kubernetes and put replica count as required.

