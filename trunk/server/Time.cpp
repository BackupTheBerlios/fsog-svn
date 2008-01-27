#include "Time.hpp"

TimeMicro nowMicro() throw()
{
  static struct timeval tv;

  gettimeofday(&tv,0);

  static TimeMicro result;

  result=tv.tv_sec;
  result*=1000000;
  result+=tv.tv_usec;

  return result;
}

TimeMicro futureMicro(const uint_fast16_t seconds) throw()
{
  static struct timeval tv;

  gettimeofday(&tv,0);

  static TimeMicro result;

  result=tv.tv_sec;
  result+=seconds;
  result*=1000000;
  result+=tv.tv_usec;

  return result;
}

