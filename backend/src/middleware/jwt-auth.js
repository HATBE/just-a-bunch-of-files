const jwt = require('jsonwebtoken');
const UsersService = require('../services/UsersService');

const usersService = new UsersService();

// there are two different cases, either the user is just not logged in or the user tries to login but the token is malformed, or expired, ...
// the first way is just not logged in -> go next, set user to null, the second is "dangerus", throw an error and don't move on

async function authJwt(req, res, next) {
   req.user = null; // set user to null as a default, if it changes later, then the user is logged in
   req.jwtclaim = null;

   if(!req.headers['authorization']) {
      // if the authorization header is not set, the user can't be logged in
      return notLoggedIn(req, next);
   }

   const bearerToken = req.headers['authorization'].split(' ')[1];

   if(!bearerToken || bearerToken == 'null') {
      // if the bearer token is falsy or null, the user can't be logged in
      return notLoggedIn(req, next);
   }

   let claim;

   try {
      claim = jwt.verify(bearerToken, process.env.AUTH_JWT_SECRET);
   } catch (err) {
      // the token cannot be verified (maybe malformed, expired?), the user can't be logged in (exact error is in error.message)
      // don't execute next(), because this is an error and the user is not just not logged in
      console.log(`[AUTH] [WARN] "${req.socket.remoteAddress}" tried to access the api with an unauthorized token! "${err.message}".`);
      return res.status(401).json({status: false, message: 'You are not authorized!', data: {error: err.message}});
   }

   if(!claim) {
       // if the claim failed, the user can't be logged in
       return notLoggedIn(req, next);
   }

   const user = await usersService.getUserById(claim.id); // get user from id saved in token

   if(!user) {
      // no user with this id was found (maybe the user was deleted while the token is still valid?), the user can't be logged in
      return notLoggedIn(req, next);
   }

   claim = {iat: claim.iat, exp: claim.exp}; // filter the properties to show the user

   req.jwtclaim = claim; // save claim data 
   req.user = user; // set the loggedin backend user to the user class with the loggedin user id

   next();
}

function notLoggedIn(req, next) {
   req.user = null;
   req.jwtclaim = null;
   return next();
}

module.exports = authJwt;