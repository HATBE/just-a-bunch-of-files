class Validators {
    static VALIDATORS = {
        USERNAME: Validators.username,
        NAME: Validators.name,
        EMAIL_ADDRESS: Validators.email,
        PASSWORD: Validators.password,
        PAGE_NUMBER: Validators.pageNumber,
        NO_VALIDATION: () => {return true;}
    }

    // TODO: check if this could/should be emplemented as middleware
    static validateMulti(validationObject, formats) {
        if(!Array.isArray(formats)) throw new Error('Formats variable is not an array!');

        let mustBeInArray = [];
        formats.forEach(format => {
            if(!format['name']) throw new Error('No name attribute!');
            if(!format['validateFor']) throw new Error('No validateFor attribute!');
            mustBeInArray.push(format.name);
        });

        const checkBodyFields = Validators.checkIfKeysExistsInArray(validationObject, mustBeInArray);

        if(!checkBodyFields.status) return {status: false, message: `There are some fields missing: "${checkBodyFields.res.join('", "')}". Please add them!`};

        for(let format of formats) {
            if(!format.validateFor(validationObject[format.name])) {
                return {status: false, message: `The "${format.name}" has a wrong format`};
            }
        }

        return {status: true, message: ''};
    }

    static checkIfKeysExistsInArray(array, keys) {
        let res = [];
        keys.forEach(key => {
            // if key is not in array -> add to res array
            if(!(key in array)) {
                res.push(key);
            }
        });
        return {status: res == 0 ? true : false, res: res};
    }

    static name(name) {
        // check if name is valid
        if(!new RegExp(/^[A-Za-zàáâäãåąčćęèéêëėįìíîïłńòóôöõøùúûüųūÿýżźñçčšžÀÁÂÄÃÅĄĆČĖĘÈÉÊËÌÍÎÏĮŁŃÒÓÔÖÕØÙÚÛÜŲŪŸÝŻŹÑßÇŒÆČŠŽ∂ð ,.'-]+$/u).test(name)) {
            return false;
        }
        // check if name is in range
        if(name.length < 1 || name.length > 254) {
            return false;
        }
        return true;
    }

    static username(username) {
        // check if username is valid
        if(!new RegExp(/^[A-Za-z0-9]+$/).test(username)) {
            return false;
        }
        // check if username is in range
        if(username.length < 1 || username.length > 24) {
            return false;
        }
        return true;
    }

    static email(email) {
        // check if email is valid
        if(!new RegExp(/(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|"(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21\x23-\x5b\x5d-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])*")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\[(?:(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9]))\.){3}(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9])|[a-z0-9-]*[a-z0-9]:(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21-\x5a\x53-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])+)\])/).test(email)) {
            return false;
        }
        // check if email is in range
        if(email.length < 1 || email.length > 319) {
            return false;
        }
        return true;
    }

    static password(password) {
        // check if password is valid
        // at least one uppercase letter, one lowercase letter, one number and one special character between 6 and 256 characters
        if(!new RegExp(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d]{6,256}$/).test(password)) {
            return false;
        }
        return true;
    }

    static pageNumber(number) {
        if(typeof number != 'string') {
            return false;
        }  
        
        if(isNaN(number)) {
            return false;
        }

        if(number <= 0) {
            return false;
        }
        return true;
    }
}   

module.exports = Validators;