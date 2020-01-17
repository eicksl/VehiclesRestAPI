package com.udacity.vehicles.service;

import com.udacity.vehicles.client.maps.MapsClient;
import com.udacity.vehicles.client.prices.Price;
import com.udacity.vehicles.client.prices.PriceClient;
import com.udacity.vehicles.domain.car.Car;
import com.udacity.vehicles.domain.car.CarRepository;

import java.util.List;
import java.util.Optional;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import javax.persistence.EntityNotFoundException;

/**
 * Implements the car service create, read, update or delete
 * information about vehicles, as well as gather related
 * location and price data when desired.
 */
@Service
public class CarService {

    private final CarRepository repository;
    private final MapsClient mapsClient;
    private final PriceClient pricingClient;

    public CarService(CarRepository repository, MapsClient maps, PriceClient pricing) {
        /**
         * TODO: Add the Maps and Pricing Web Clients you create
         *   in `VehiclesApiApplication` as arguments and set them here.
         */
        this.repository = repository;
        this.mapsClient = maps;
        this.pricingClient = pricing;
    }

    /**
     * Gathers a list of all vehicles
     * @return a list of all vehicles in the CarRepository
     */
    public List<Car> list() {
        return repository.findAll();
    }

    /**
     * Gets car information by ID (or throws exception if non-existent)
     * @param id the ID number of the car to gather information on
     * @return the requested car's information, including location and price
     */
    public Car findById(Long id) {
        /**
         * TODO: Find the car by ID from the `repository` if it exists.
         *   If it does not exist, throw a CarNotFoundException
         *   Remove the below code as part of your implementation.
         */
        //System.out.println(Arrays.toString(list().toArray()));
        /*
        Car car;
        try {
            car = repository.getOne(id);
        }
        catch (EntityNotFoundException e) {
            throw new CarNotFoundException();
        }
        */
        Optional<Car> optionalCar = repository.findById(id);
        Car car;
        if (optionalCar.isPresent()) {
            car = optionalCar.get();
        }
        else {
            throw new CarNotFoundException();
        }

        /**
         * TODO: Use the Pricing Web client you create in `VehiclesApiApplication`
         *   to get the price based on the `id` input'
         * TODO: Set the price of the car
         * Note: The car class file uses @transient, meaning you will need to call
         *   the pricing service each time to get the price.
         */
        car.setPrice(pricingClient.getPrice(id));
        /*
        Price price = pricingClient.get()
                .uri(uriBuilder -> uriBuilder
                .path("/services/price")
                .queryParam("vehicleId", id)
                .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Price.class)
                .block();

        System.out.println(price.price);
         */

        /**
         * TODO: Use the Maps Web client you create in `VehiclesApiApplication`
         *   to get the address for the vehicle. You should access the location
         *   from the car object and feed it to the Maps service.
         * TODO: Set the location of the vehicle, including the address information
         * Note: The Location class file also uses @transient for the address,
         * meaning the Maps service needs to be called each time for the address.
         */
        car.setLocation(mapsClient.getAddress(car.getLocation()));

        return car;
    }

    /**
     * Either creates or updates a vehicle, based on prior existence of car
     * @param car A car object, which can be either new or existing
     * @return the new/updated car is stored in the repository
     */
    public Car save(Car car) {
        if (car.getId() != null) {
            return repository.findById(car.getId())
                    .map(carToBeUpdated -> {
                        carToBeUpdated.setDetails(car.getDetails());
                        carToBeUpdated.setLocation(car.getLocation());
                        return repository.save(carToBeUpdated);
                    }).orElseThrow(CarNotFoundException::new);
        }

        return repository.save(car);
    }

    /**
     * Deletes a given car by ID
     * @param id the ID number of the car to delete
     */
    public void delete(Long id) {
        /**
         * TODO: Find the car by ID from the `repository` if it exists.
         *   If it does not exist, throw a CarNotFoundException
         */
        Car car;
        try {
            car = repository.getOne(id);
        }
        catch (EntityNotFoundException e) {
            throw new CarNotFoundException();
        }


        /**
         * TODO: Delete the car from the repository.
         */
        repository.delete(car);

    }
}
